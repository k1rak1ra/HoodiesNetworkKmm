package net.k1ra.hoodies_network_kmm

import kotlinx.coroutines.runBlocking
import net.k1ra.hoodies_network_kmm.config.HttpClientConfig
import net.k1ra.hoodies_network_kmm.interceptor.Interceptor
import net.k1ra.hoodies_network_kmm.mockwebserver.ServerManager
import net.k1ra.hoodies_network_kmm.request.RetryableCancellableMutableRequest
import net.k1ra.hoodies_network_kmm.result.Failure
import net.k1ra.hoodies_network_kmm.result.HttpClientError
import net.k1ra.hoodies_network_kmm.result.Success
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class RetryTests {

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
    fun retryRequest() {
        runBlocking {
            var runs = 0

            val interceptor = object : Interceptor() {
                override suspend fun interceptError(
                    error: HttpClientError,
                    retryableCancellableMutableRequest: RetryableCancellableMutableRequest,
                    autoRetryAttempts: Int
                ) {
                    runs = autoRetryAttempts
                }
            }

            val client = HoodiesNetworkClient.Builder().apply {
                baseUrl = ServerManager.testBaseUrl
                interceptors = listOf(interceptor)
                retryOnConnectionFailure = true
                maxRetryLimit = 5
                config = HttpClientConfig(1.seconds, 1.seconds)
            }.build()

            when (client.get<String>("echo/10")) {
                is Success -> {
                    throw Exception("This request should've timed out")
                }
                is Failure -> {
                    assertEquals("Unexpected number of retry runs", runs, 5)
                }
            }
        }
    }
}