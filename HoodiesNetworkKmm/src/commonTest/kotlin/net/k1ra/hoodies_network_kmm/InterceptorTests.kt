package net.k1ra.hoodies_network_kmm

import kotlinx.coroutines.runBlocking
import net.k1ra.hoodies_network_kmm.config.HttpClientConfig
import net.k1ra.hoodies_network_kmm.interceptor.Interceptor
import net.k1ra.hoodies_network_kmm.mockwebserver.ServerManager
import net.k1ra.hoodies_network_kmm.request.CancellableMutableRequest
import net.k1ra.hoodies_network_kmm.request.RetryableCancellableMutableRequest
import net.k1ra.hoodies_network_kmm.result.Failure
import net.k1ra.hoodies_network_kmm.result.HttpClientError
import net.k1ra.hoodies_network_kmm.result.Success
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class InterceptorTests {

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
    fun cancelRequestInInterceptRequestTest() {
        runBlocking {
            val interceptor = object: Interceptor() {
                override suspend fun interceptRequest(identifier: String, cancellableMutableRequest: CancellableMutableRequest) {
                    cancellableMutableRequest.cancelRequest(Success("Cancelled!"))
                }
            }

            val client = HoodiesNetworkClient.Builder().apply {
                baseUrl = ServerManager.testBaseUrl
                interceptors = listOf(interceptor)
            }.build()

            when (val result = client.get<String>("echo/10")) {
                is Success -> {
                    assertEquals("Request should've been cancelled", result.value, "Cancelled!")
                }
                is Failure -> {
                    throw Exception("Request wasn't cancelled!")
                }
            }
        }
    }

    @Test
    fun cancelFailedRequestTest() {
        runBlocking {
            val interceptor = object: Interceptor() {
                override suspend fun interceptError(
                    error: HttpClientError,
                    retryableCancellableMutableRequest: RetryableCancellableMutableRequest,
                    autoRetryAttempts: Int
                ) {
                   retryableCancellableMutableRequest.cancelRequest(Success("Cancelled!"))
                }
            }

            val client = HoodiesNetworkClient.Builder().apply {
                baseUrl = ServerManager.testBaseUrl
                interceptors = listOf(interceptor)
                config = HttpClientConfig(1.seconds, 1.seconds)
            }.build()

            when (val result = client.get<String>("echo/10")) {
                is Success -> {
                    assertEquals("Request should've been cancelled", result.value, "Cancelled!")
                }
                is Failure -> {
                    throw Exception("Request wasn't cancelled!")
                }
            }
        }
    }

    @Test
    fun retryRequestTest() {
        runBlocking {
            var counter = 0
            val interceptor = object: Interceptor() {
                override suspend fun interceptError(
                    error: HttpClientError,
                    retryableCancellableMutableRequest: RetryableCancellableMutableRequest,
                    autoRetryAttempts: Int
                ) {
                    if (error.code == 401) {
                        val headers = retryableCancellableMutableRequest.request.headers
                        headers["key"] = counter++.toString()
                        retryableCancellableMutableRequest.request.headers = headers

                        retryableCancellableMutableRequest.retryRequest()
                    }
                }
            }

            val client = HoodiesNetworkClient.Builder().apply {
                baseUrl = ServerManager.testBaseUrl
                interceptors = listOf(interceptor)
            }.build()

            when (val result = client.get<String>("wants_key")) {
                is Success -> {
                    assertEquals("Success result was not returned", result.value, "Success!")
                }
                is Failure -> {
                    throw Exception(result.reason)
                }
            }
        }
    }
}