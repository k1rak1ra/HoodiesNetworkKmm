package net.k1ra.hoodies_network_kmm

import com.benasher44.uuid.uuid4
import kotlinx.coroutines.runBlocking
import net.k1ra.hoodies_network_kmm.mockwebserver.MockWebServerManager
import net.k1ra.hoodies_network_kmm.mockwebserver.ServerManager
import net.k1ra.hoodies_network_kmm.mockwebserver.helper.MockServerMaker
import net.k1ra.hoodies_network_kmm.result.Failure
import net.k1ra.hoodies_network_kmm.result.Success
import net.k1ra.hoodies_network_kmm.testObjects.CookieFactoryRequest
import net.k1ra.hoodies_network_kmm.testObjects.Headers
import net.k1ra.hoodies_network_kmm.testObjects.HttpBinResponse
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.Test

class MockServerMakerTests {

    @Test
    fun testMockServerMakerJsonBody() {
        runBlocking {
            val request = HttpBinResponse(
                Headers(uuid4().toString()),
                uuid4().toString(),
                uuid4().toString(),
                null
            )

            val expectedResponse = CookieFactoryRequest(uuid4().toString(), uuid4().toString())

            val headers = mutableMapOf(uuid4().toString() to uuid4().toString(), uuid4().toString() to uuid4().toString())

            val serverBuilder = MockWebServerManager.Builder()

            MockServerMaker.Builder()
                .acceptMethod("POST")
                .expect(request)
                .expectHeaders(headers)
                .returnThisObjectIfInputMatches(expectedResponse)
                .applyToMockWebServerBuilder("/test", serverBuilder)

            val server = serverBuilder.start()

            val client = HoodiesNetworkClient.Builder().apply {
                baseUrl = ServerManager.testBaseUrl
            }.build()

            when (val result = client.post<CookieFactoryRequest, HttpBinResponse>("test", request, headers)) {
                is Success -> {
                    assertEquals("Did not get expected response - name", result.value.name, expectedResponse.name)
                    assertEquals("Did not get expected response - value", result.value.value, expectedResponse.value)
                    server.stop()
                }
                is Failure -> {
                    throw result.reason
                }
            }
        }
    }

    @Test
    fun testMockServerMakerUrlQueryParam() {
        runBlocking {
            val request = mutableMapOf(uuid4().toString() to uuid4().toString(), uuid4().toString() to uuid4().toString())

            val expectedResponse = CookieFactoryRequest(uuid4().toString(), uuid4().toString())

            val headers = mutableMapOf(uuid4().toString() to uuid4().toString(), uuid4().toString() to uuid4().toString())

            val serverBuilder = MockWebServerManager.Builder()

            MockServerMaker.Builder()
                .acceptMethod("POST")
                .expect(request)
                .expectHeaders(headers)
                .returnThisObjectIfInputMatches(expectedResponse)
                .applyToMockWebServerBuilder("/test", serverBuilder)

            val server = serverBuilder.start()

            val client = HoodiesNetworkClient.Builder().apply {
                baseUrl = ServerManager.testBaseUrl
            }.build()

            when (val result = client.post<CookieFactoryRequest>("test", request, headers)) {
                is Success -> {
                    assertEquals("Did not get expected response - name", result.value.name, expectedResponse.name)
                    assertEquals("Did not get expected response - value", result.value.value, expectedResponse.value)
                    server.stop()
                }
                is Failure -> {
                    throw result.reason
                }
            }
        }
    }

    @Test
    fun testMockServerMakerErrors() {
        runBlocking {
            val request = mutableMapOf(uuid4().toString() to uuid4().toString(), uuid4().toString() to uuid4().toString())

            val expectedResponse = CookieFactoryRequest(uuid4().toString(), uuid4().toString())

            val headers = mutableMapOf(uuid4().toString() to uuid4().toString(), uuid4().toString() to uuid4().toString())

            val serverBuilder = MockWebServerManager.Builder()

            MockServerMaker.Builder()
                .acceptMethod("GET")
                .expect(request)
                .expectHeaders(headers)
                .returnThisObjectIfInputMatches(expectedResponse)
                .returnErrorIfInputDoesNotMatch(403, "errorMessage")
                .applyToMockWebServerBuilder("/test", serverBuilder)

            val server = serverBuilder.start()

            val client = HoodiesNetworkClient.Builder().apply {
                baseUrl = ServerManager.testBaseUrl
            }.build()

            when (val result = client.get<CookieFactoryRequest>("test")) {
                is Success -> {
                    throw Exception("This should've not succeeded!")
                }
                is Failure -> {
                    assertEquals("Error code does not match", result.reason.code, 403)
                    assertEquals("Error message does not match", result.reason.message, "errorMessage")
                    server.stop()
                }
            }
        }
    }
}