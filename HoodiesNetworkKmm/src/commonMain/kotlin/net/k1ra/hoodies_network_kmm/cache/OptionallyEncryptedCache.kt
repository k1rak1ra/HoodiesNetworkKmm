package net.k1ra.hoodies_network_kmm.cache

import net.k1ra.hoodies_network_kmm.cache.configuration.CacheEnabled
import net.k1ra.hoodies_network_kmm.cryptography.CipherMode
import net.k1ra.hoodies_network_kmm.cryptography.Cryptography
import net.k1ra.hoodies_network_kmm.database.DatabaseFactory
import net.k1ra.hoodies_network_kmm.request.NetworkRequest
import net.k1ra.hoodies_network_kmm.request.NetworkResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import net.k1ra.hoodies_network_kmm.core.CustomDispatchers
import kotlin.time.Duration.Companion.seconds

class OptionallyEncryptedCache(private val cacheConfig: CacheEnabled) {
    private val db = DatabaseFactory.provideCacheDatabase()

    fun cacheRequestResult(result: ByteArray, request: NetworkRequest) = CoroutineScope(
        CustomDispatchers.IO).launch {
        var iv: ByteArray? = Cryptography.generateIv()

        val cachedResult = if (cacheConfig.encryptionEnabled) {
            //Make sure the IV is unique
            while (db.getByIv(iv).executeAsOne() > 0)
                iv = Cryptography.generateIv()

            //Encrypt the data
            Cryptography.runAes(result, iv!!, CipherMode.ENCRYPT)
        } else {
            iv = null
            result
        }

        db.delete(request.url, request.body.contentHashCode().toLong())
        db.insert(
            request.url,
            request.body.contentHashCode().toLong(),
            getCurrentSeconds(),
            cachedResult,
            iv
        )
    }

    fun getCachedData(request: NetworkRequest) : NetworkResponse {
        val cachedData = db.get(request.url, request.body.contentHashCode().toLong()).executeAsOne()

        val data = if (cachedData.iv != null) {
            Cryptography.runAes(cachedData.data_, cachedData.iv, CipherMode.DECRYPT)
        } else {
            cachedData.data_
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

    fun isDataStale(request: NetworkRequest): Boolean {
        val data = db.get(request.url, request.body.contentHashCode().toLong()).executeAsOneOrNull()

        return data == null || (getCurrentSeconds() - data.cachedAt).seconds > cacheConfig.staleDataThreshold
    }
}