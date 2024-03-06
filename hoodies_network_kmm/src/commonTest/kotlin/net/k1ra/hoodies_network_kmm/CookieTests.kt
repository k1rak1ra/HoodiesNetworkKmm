package net.k1ra.hoodies_network_kmm

import com.benasher44.uuid.uuid4
import kotlinx.coroutines.runBlocking
import net.k1ra.hoodies_network_kmm.config.HttpClientConfig
import net.k1ra.hoodies_network_kmm.cookies.CookieJar
import net.k1ra.hoodies_network_kmm.cookies.HttpCookie
import net.k1ra.hoodies_network_kmm.mockwebserver.ServerManager
import net.k1ra.hoodies_network_kmm.result.Failure
import net.k1ra.hoodies_network_kmm.result.HttpClientError
import net.k1ra.hoodies_network_kmm.result.Success
import net.k1ra.hoodies_network_kmm.testObjects.CookieFactoryRequest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.Test

class CookieTests {

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
    fun cookieTestNonPersistent() {
        runBlocking {
            val localCookieJar = CookieJar()
            localCookieJar.removeAll()
            val client = HoodiesNetworkClient.Builder().apply {
                baseUrl = ServerManager.testBaseUrl
                cookieJar = localCookieJar
            }.build()

            //Request 3 random cookies from the CookieFactory
            val request = arrayListOf<CookieFactoryRequest>()
            request.add(CookieFactoryRequest(uuid4().toString(), uuid4().toString()))
            request.add(CookieFactoryRequest(uuid4().toString(), uuid4().toString()))
            request.add(CookieFactoryRequest(uuid4().toString(), uuid4().toString()))

            when (val result = client.post<String, ArrayList<CookieFactoryRequest>>("/cookie_factory", request)) {
                is Success -> {

                    //Add an extra cookie directly to the jar
                    val newKey = uuid4().toString()
                    val newValue = uuid4().toString()
                    request.add(CookieFactoryRequest(newKey, newValue))
                    localCookieJar.add(HttpCookie(newKey, newValue))

                    //Confirm the cookies in the jar
                    val cookies = localCookieJar.get()
                    assertEquals("Cookie list size mismatch", cookies.size, 4)
                    for (i in 0 until 4) {
                        assertEquals("Cookie contents mismatch", cookies[i].value, request.firstOrNull { it.name == cookies[i].name}?.value)
                    }

                    //The cookies should now be stored
                    //To verify this, we make a request to CookieInspector in order to get our cookies back
                    when (val inspectorResult = client.post<ArrayList<CookieFactoryRequest>>("/cookie_inspector")) {
                        is Success -> {
                            for (item in inspectorResult.value) {
                                assertEquals("Cookies returned by cookie inspector do not match after removal", item.value,
                                    request.firstOrNull { it.name == item.name}?.value)
                            }

                            //Ensure we can delete a cookie
                            val cookieToDelete = request[1].name
                            localCookieJar.remove(cookieToDelete)
                            for (item in inspectorResult.value) {
                                assertEquals("Cookies returned by cookie inspector do not match after removal", item.value,
                                    request.firstOrNull { it.name == item.name}?.value)
                            }

                            //Finally, ensure that full cookie jar deletion works
                            localCookieJar.removeAll()
                            assertEquals("Cookie jar should be empty", localCookieJar.get().size, 0)

                        }
                        is Failure -> {
                            throw inspectorResult.reason
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
    fun noCookieTest() {
        runBlocking {
            val client = HoodiesNetworkClient.Builder().apply {
                baseUrl = ServerManager.testBaseUrl
            }.build()

            //Request 3 random cookies from the CookieFactory
            val request = arrayListOf<CookieFactoryRequest>()
            request.add(CookieFactoryRequest(uuid4().toString(), uuid4().toString()))
            request.add(CookieFactoryRequest(uuid4().toString(), uuid4().toString()))
            request.add(CookieFactoryRequest(uuid4().toString(), uuid4().toString()))

            when (val result = client.post<String, ArrayList<CookieFactoryRequest>>("/cookie_factory", request)) {
                is Success -> {

                    //The cookies should now be stored
                    //To verify this, we make a request to CookieInspector in order to get our cookies back
                    when (val inspectorResult = client.post<ArrayList<CookieFactoryRequest>>("/cookie_inspector")) {
                        is Success -> {
                            assertEquals("Cookies are not supposed to be present", inspectorResult.value.size, 0)
                        }
                        is Failure -> {
                            throw inspectorResult.reason
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