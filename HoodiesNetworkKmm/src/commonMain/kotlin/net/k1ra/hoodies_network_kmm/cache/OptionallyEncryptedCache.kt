package net.k1ra.hoodies_network_kmm.cache

import net.k1ra.hoodies_network_kmm.cache.configuration.CacheEnabled
import net.k1ra.hoodies_network_kmm.cryptography.CipherMode
import net.k1ra.hoodies_network_kmm.cryptography.Cryptography
import net.k1ra.hoodies_network_kmm.request.NetworkRequest
import net.k1ra.hoodies_network_kmm.request.NetworkResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.k1ra.hoodies_network_kmm.cache.database.CacheDao
import net.k1ra.hoodies_network_kmm.cache.database.CacheEntity
import net.k1ra.hoodies_network_kmm.cache.database.buildCacheDatabase
import net.k1ra.hoodies_network_kmm.core.CustomDispatchers
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

class OptionallyEncryptedCache(private val cacheConfig: CacheEnabled) {
    companion object {
        private var db: CacheDao? = null
        private val dbMutex = Mutex()
        private val writeMutex = Mutex()

        suspend fun getDb() : CacheDao {
            db?.let { return it }
            return dbMutex.withLock {
                db ?: buildCacheDatabase().getDao().also { db = it }
            }
        }
    }

    fun cacheRequestResult(result: ByteArray, request: NetworkRequest) = CoroutineScope(CustomDispatchers.IO).launch {
        var iv: ByteArray? = Cryptography.generateIv()

        val cachedResult = if (cacheConfig.encryptionEnabled) {
            //Make sure the IV is unique
            while (getDb().getByIv(iv) > 0)
                iv = Cryptography.generateIv()

            //Encrypt the data
            Cryptography.runAes(result, iv!!, CipherMode.ENCRYPT)
        } else {
            iv = null
            result
        }

        writeMutex.withLock {
            getDb().delete(request.url, request.body.contentHashCode().toLong())
            getDb().insert(
                CacheEntity(
                    url = request.url,
                    bodyHash = request.body.contentHashCode().toLong(),
                    cachedAt = getCurrentSeconds(),
                    data = cachedResult,
                    iv = iv
                )
            )
        }
    }

    suspend fun getCachedData(request: NetworkRequest) : NetworkResponse {
        val cachedData = getDb().get(request.url, request.body.contentHashCode().toLong())!!

        val data = if (cachedData.iv != null) {
            Cryptography.runAes(cachedData.data, cachedData.iv, CipherMode.DECRYPT)
        } else {
            cachedData.data
        }

        return NetworkResponse(
            request,
            0,
            data,
            200,
            mutableMapOf()
        )
    }

    private fun getCurrentSeconds(): Long {
        return Clock.System.now().epochSeconds
    }

    suspend fun isDataStale(request: NetworkRequest): Boolean {
        val data = getDb().get(request.url, request.body.contentHashCode().toLong())

        return data == null || (getCurrentSeconds() - data.cachedAt).seconds > cacheConfig.staleDataThreshold
    }
}