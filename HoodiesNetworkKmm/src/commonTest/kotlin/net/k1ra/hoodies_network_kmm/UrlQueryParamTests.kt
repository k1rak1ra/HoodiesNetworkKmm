package net.k1ra.hoodies_network_kmm

import com.benasher44.uuid.uuid4
import kotlinx.coroutines.runBlocking
import net.k1ra.hoodies_network_kmm.config.HttpClientConfig
import net.k1ra.hoodies_network_kmm.mockwebserver.ServerManager
import net.k1ra.hoodies_network_kmm.result.Failure
import net.k1ra.hoodies_network_kmm.result.Success
import net.k1ra.hoodies_network_kmm.testObjects.HttpBinResponse
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.Test

class UrlQueryParamTests {

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
    fun urlQueryPost() {
        runBlocking {
            val params = mapOf("test1" to uuid4().toString(), "test2" to uuid4().toString(), "test3" to uuid4().toString())

            val client = HoodiesNetworkClient.Builder().apply {
                baseUrl = ServerManager.testBaseUrl
            }.build()

            when (val result = client.post<HttpBinResponse>("/post", params)) {
                is Success -> {
                    assertEquals("test1 did not match", params["test1"], result.value.args?.test1)
                    assertEquals("test2 did not match", params["test2"], result.value.args?.test2)
                    assertEquals("test3 did not match", params["test3"], result.value.args?.test3)
                }
                is Failure -> {
                    throw result.reason
                }
            }
        }
    }

    @Test
    fun urlQueryGet() {
        runBlocking {
            val params = mapOf("test1" to uuid4().toString(), "test2" to uuid4().toString(), "test3" to uuid4().toString())

            val client = HoodiesNetworkClient.Builder().apply {
                baseUrl = ServerManager.testBaseUrl
            }.build()

            when (val result = client.get<HttpBinResponse>("/get", params)) {
                is Success -> {
                    assertEquals("test1 did not match", params["test1"], result.value.args?.test1)
                    assertEquals("test2 did not match", params["test2"], result.value.args?.test2)
                    assertEquals("test3 did not match", params["test3"], result.value.args?.test3)
                }
                is Failure -> {
                    throw result.reason
                }
            }
        }
    }
}