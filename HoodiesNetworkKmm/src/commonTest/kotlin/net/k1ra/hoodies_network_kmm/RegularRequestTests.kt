package net.k1ra.hoodies_network_kmm

import com.benasher44.uuid.uuid4
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.k1ra.hoodies_network_kmm.config.HttpClientConfig
import net.k1ra.hoodies_network_kmm.mockwebserver.ServerManager
import net.k1ra.hoodies_network_kmm.result.Failure
import net.k1ra.hoodies_network_kmm.result.Success
import net.k1ra.hoodies_network_kmm.testObjects.CookieFactoryRequest
import net.k1ra.hoodies_network_kmm.testObjects.HttpBinResponse
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.Test

class RegularRequestTests {
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
    fun testPost() {
        runBlocking {
            val request = CookieFactoryRequest(uuid4().toString(), uuid4().toString())

            val client = HoodiesNetworkClient.Builder().apply {
                baseUrl = ServerManager.testBaseUrl
            }.build()

            when (val result = client.post<HttpBinResponse, CookieFactoryRequest>("/post", request)) {
                is Success -> {
                    assertEquals("Body did not match expected", kotlinJson.encodeToString(request), result.value.data)
                }
                is Failure -> {
                    throw result.reason
                }
            }
        }
    }

    @Test
    fun testPut() {
        runBlocking {
            val request = CookieFactoryRequest(uuid4().toString(), uuid4().toString())

            val client = HoodiesNetworkClient.Builder().apply {
                baseUrl = ServerManager.testBaseUrl
            }.build()

            when (val result = client.put<HttpBinResponse, CookieFactoryRequest>("/put", request)) {
                is Success -> {
                    assertEquals("Body did not match expected", kotlinJson.encodeToString(request), result.value.data)
                }
                is Failure -> {
                    throw result.reason
                }
            }
        }
    }

    @Test
    fun testDelete() {
        runBlocking {
            val request = CookieFactoryRequest(uuid4().toString(), uuid4().toString())

            val client = HoodiesNetworkClient.Builder().apply {
                baseUrl = ServerManager.testBaseUrl
            }.build()

            when (val result = client.delete<HttpBinResponse, CookieFactoryRequest>("/delete", request)) {
                is Success -> {
                    assertEquals("Body did not match expected", kotlinJson.encodeToString(request), result.value.data)
                }
                is Failure -> {
                    throw result.reason
                }
            }
        }
    }

    @Test
    fun testDeleteUnit() {
        runBlocking {
            val client = HoodiesNetworkClient.Builder().apply {
                baseUrl = ServerManager.testBaseUrl
            }.build()

            when (val result = client.delete<HttpBinResponse>("/delete")) {
                is Success -> {
                    assertEquals("Body did not match expected", "", result.value.data)
                }
                is Failure -> {
                    throw result.reason
                }
            }
        }
    }
}