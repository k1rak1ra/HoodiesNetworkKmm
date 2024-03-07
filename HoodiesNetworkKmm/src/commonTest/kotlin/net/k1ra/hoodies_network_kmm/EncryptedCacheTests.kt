@file:Suppress("NAME_SHADOWING")

package net.k1ra.hoodies_network_kmm

import com.benasher44.uuid.uuid4
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import net.k1ra.hoodies_network_kmm.cache.configuration.CacheEnabled
import net.k1ra.hoodies_network_kmm.config.HttpClientConfig
import net.k1ra.hoodies_network_kmm.cryptography.CipherMode
import net.k1ra.hoodies_network_kmm.cryptography.Cryptography
import net.k1ra.hoodies_network_kmm.database.DatabaseFactory
import net.k1ra.hoodies_network_kmm.mockwebserver.ServerManager
import net.k1ra.hoodies_network_kmm.result.Failure
import net.k1ra.hoodies_network_kmm.result.Success
import net.k1ra.hoodies_network_kmm.testObjects.HttpBinResponse
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class EncryptedCacheTests {
    private val kotlinJson = Json { ignoreUnknownKeys = true }

    @BeforeTest
    fun startServer() {
        HttpClientConfig.testMode = true
        ServerManager.start()
    }

    @AfterTest
    fun stopServer() {
        ServerManager.stop()
    }

    @Test
    fun makeSureDataWasCached() {
        runBlocking {
            val db = DatabaseFactory.provideCacheDatabase()
            val testData = uuid4().toString()

            val client = HoodiesNetworkClient.Builder().apply {
                baseUrl = ServerManager.testBaseUrl
                cacheConfiguration = CacheEnabled()
            }.build()

            when (val result = client.post<HttpBinResponse, String>("post", testData)) {
                is Success -> {
                    //Assert that we got the parameters we were expecting back
                    assertEquals("Did not get test data back", result.value.data, "\"$testData\"")

                    val url = "${ServerManager.testBaseUrl}/post"
                    //Wait for cache data to be written on another thread
                    delay(1000)

                    //Get the cached data
                    val cachedData = db.get(url, "\"$testData\"".encodeToByteArray().contentHashCode().toLong()).executeAsOne()
                    var data = cachedData.data_.decodeToString()

                    //Convert it to an object
                    val cachedObj = kotlinJson.decodeFromString<HttpBinResponse>(data)

                    //Assert that the cached object equals the object in the original server response
                    assertEquals("Received data does not match cache", result.value.data, cachedObj.data)

                    //Now, we replace the data in the cache with a modified version
                    data = data.replace(testData, "cacheModified")
                    db.delete(url, "\"$testData\"".encodeToByteArray().contentHashCode().toLong())
                    db.insert(
                        cachedData.url,
                        cachedData.bodyHash,
                        cachedData.cachedAt,
                        data.encodeToByteArray(),
                        cachedData.iv
                    )

                    //Now, we make the same network call again but make sure the data is fetched from the cache
                    when (val resultFromCache = client.post<HttpBinResponse, String>("post", testData)) {
                        is Success -> {
                            //Assert that the result we got came from the cache
                            assertEquals("Did not get modified result from cache", resultFromCache.value.data, "\"cacheModified\"")

                            //Now, we wait 2 seconds
                            delay(2000)

                            //And finally, make another request, but this time make sure the data is stale and ensure the cached data is not fetched
                            when (val result = client.post<HttpBinResponse, String>("post", testData, customCache = CacheEnabled(staleDataThreshold = 1.seconds))) {
                                is Success -> {
                                    //Assert that we got the parameters we were expecting back
                                    assertEquals("Did not get original test data back", result.value.data, "\"$testData\"")

                                    //Wait for cache data to be written on another thread
                                    delay(1000)

                                    //Get the cached data
                                    val cachedData = db.get(url, "\"$testData\"".encodeToByteArray().contentHashCode().toLong()).executeAsOne()
                                    val data = cachedData.data_.decodeToString()

                                    //Convert it to an object
                                    val cachedObj = kotlinJson.decodeFromString<HttpBinResponse>(data)

                                    //Assert that the cached object equals the object in the original server response
                                    assertEquals("Received data does not match cache", result.value.data, cachedObj.data)
                                }
                                is Failure -> {
                                    throw result.reason
                                }
                            }

                        }
                        is Failure -> {
                            throw resultFromCache.reason
                        }
                    }

                }
                is Failure -> {
                    throw result.reason
                }
            }
        }
    }

    @Test
    fun makeSureDataWasCachedEncryptedVersion() {
        runBlocking {
            val db = DatabaseFactory.provideCacheDatabase()
            val testData = uuid4().toString()

            val client = HoodiesNetworkClient.Builder().apply {
                baseUrl = ServerManager.testBaseUrl
                cacheConfiguration = CacheEnabled(encryptionEnabled = true)
            }.build()

            when (val result = client.post<HttpBinResponse, String>("post", testData)) {
                is Success -> {
                    //Assert that we got the parameters we were expecting back
                    assertEquals("Did not get test data back", result.value.data, "\"$testData\"")

                    val url = "${ServerManager.testBaseUrl}/post"
                    //Wait for cache data to be written on another thread
                    delay(1000)

                    //Get the cached data
                    val cachedData = db.get(url, "\"$testData\"".encodeToByteArray().contentHashCode().toLong()).executeAsOne()
                    var data = Cryptography.runAes(cachedData.data_, cachedData.iv!!, CipherMode.DECRYPT).decodeToString()

                    //Convert it to an object
                    val cachedObj = kotlinJson.decodeFromString<HttpBinResponse>(data)

                    //Assert that the cached object equals the object in the original server response
                    assertEquals("Received data does not match cache", result.value.data, cachedObj.data)

                    //Now, we replace the data in the cache with a modified version
                    data = data.replace(testData, "cacheModified")
                    db.delete(url, "\"$testData\"".encodeToByteArray().contentHashCode().toLong())
                    db.insert(
                        cachedData.url,
                        cachedData.bodyHash,
                        cachedData.cachedAt,
                        Cryptography.runAes(data.encodeToByteArray(), cachedData.iv!!, CipherMode.ENCRYPT),
                        cachedData.iv
                    )

                    //Now, we make the same network call again but make sure the data is fetched from the cache
                    when (val resultFromCache = client.post<HttpBinResponse, String>("post", testData)) {
                        is Success -> {
                            //Assert that the result we got came from the cache
                            assertEquals("Did not get modified result from cache", resultFromCache.value.data, "\"cacheModified\"")

                            //Now, we wait 2 seconds
                            delay(2000)

                            //And finally, make another request, but this time make sure the data is stale and ensure the cached data is not fetched
                            when (val result = client.post<HttpBinResponse, String>("post", testData,
                                customCache = CacheEnabled(staleDataThreshold = 1.seconds, encryptionEnabled = true))) {
                                is Success -> {
                                    //Assert that we got the parameters we were expecting back
                                    assertEquals("Did not get original test data back", result.value.data, "\"$testData\"")

                                    //Wait for cache data to be written on another thread
                                    delay(1000)

                                    //Get the cached data
                                    val cachedData = db.get(url, "\"$testData\"".encodeToByteArray().contentHashCode().toLong()).executeAsOne()
                                    val data = Cryptography.runAes(cachedData.data_, cachedData.iv!!, CipherMode.DECRYPT).decodeToString()

                                    //Convert it to an object
                                    val cachedObj = kotlinJson.decodeFromString<HttpBinResponse>(data)

                                    //Assert that the cached object equals the object in the original server response
                                    assertEquals("Received data does not match cache", result.value.data, cachedObj.data)
                                }
                                is Failure -> {
                                    throw result.reason
                                }
                            }

                        }
                        is Failure -> {
                            throw resultFromCache.reason
                        }
                    }

                }
                is Failure -> {
                    throw result.reason
                }
            }
        }
    }
}
