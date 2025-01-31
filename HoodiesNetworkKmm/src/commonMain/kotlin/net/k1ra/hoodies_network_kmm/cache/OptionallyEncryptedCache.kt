package net.k1ra.hoodies_network_kmm.cache

import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
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
import net.k1ra.hoodiesnetworkkmm.database.HoodiesNetworkCacheDatabaseQueries
import kotlin.time.Duration.Companion.seconds

class OptionallyEncryptedCache(private val cacheConfig: CacheEnabled) {
    private var db: HoodiesNetworkCacheDatabaseQueries? = null

    private suspend fun getDb() : HoodiesNetworkCacheDatabaseQueries {
        if (db == null)
            db = DatabaseFactory.provideCacheDatabase()
        return db!!
    }

    suspend fun cacheRequestResult(result: ByteArray, request: NetworkRequest) = CoroutineScope(
        CustomDispatchers.IO).launch {
        var iv: ByteArray? = Cryptography.generateIv()

        val cachedResult = if (cacheConfig.encryptionEnabled) {
            //Make sure the IV is unique
            while (getDb().getByIv(iv).awaitAsOne() > 0)
                iv = Cryptography.generateIv()

            //Encrypt the data
            Cryptography.runAes(result, iv!!, CipherMode.ENCRYPT)
        } else {
            iv = null
            result
        }

        getDb().delete(request.url, request.body.contentHashCode().toLong())
        getDb().insert(
            request.url,
            request.body.contentHashCode().toLong(),
            getCurrentSeconds(),
            cachedResult,
            iv
        )
    }

    suspend fun getCachedData(request: NetworkRequest) : NetworkResponse {
        val cachedData = getDb().get(request.url, request.body.contentHashCode().toLong()).awaitAsOne()

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

    suspend fun isDataStale(request: NetworkRequest): Boolean {
        val data = getDb().get(request.url, request.body.contentHashCode().toLong()).awaitAsOneOrNull()

        return data == null || (getCurrentSeconds() - data.cachedAt).seconds > cacheConfig.staleDataThreshold
    }
}