package net.k1ra.hoodies_network_kmm.core

import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.Clock
import net.k1ra.hoodies_network_kmm.config.HttpClientConfig
import net.k1ra.hoodies_network_kmm.cookies.HttpCookie
import net.k1ra.hoodies_network_kmm.request.NetworkRequest
import net.k1ra.hoodies_network_kmm.request.NetworkResponse
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.fetch.CORS
import org.w3c.fetch.FOLLOW
import org.w3c.fetch.Headers
import org.w3c.fetch.NO_CACHE
import org.w3c.fetch.NO_CORS
import org.w3c.fetch.OMIT
import org.w3c.fetch.RequestCache
import org.w3c.fetch.RequestCredentials
import org.w3c.fetch.RequestInit
import org.w3c.fetch.RequestMode
import org.w3c.fetch.RequestRedirect
import org.w3c.fetch.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.js.Promise

actual class BaseNetwork actual constructor() {
    actual suspend fun execRequest(request: NetworkRequest, config: HttpClientConfig): NetworkResponse = suspendCancellableCoroutine { continuation ->
        val timeStart = Clock.System.now().toEpochMilliseconds()
        val response = NetworkResponse(
            request,
            0,
            byteArrayOf(),
            -1,
            mutableMapOf()
        )

        //Apply request headers
        val requestHeaders = Headers()
        request.headers.forEach {
            requestHeaders.append(it.key, it.value)
        }

        var body: Uint8Array? = null
        request.body?.let { body = it.asJsArray() }

        try {
            val w3cRequest = RequestInit(
                method = request.method,
                headers = requestHeaders,
                body = body,
                cache = RequestCache.NO_CACHE,
                credentials = RequestCredentials.OMIT,
                mode = RequestMode.CORS,
                redirect = RequestRedirect.FOLLOW,
                referrerPolicy = "same-origin".toJsString()
            )

            window.fetch(request.url, w3cRequest).then {
                response.statusCode = it.status.toInt()

                val respBody = it.arrayBuffer().then { buf ->
                    response.data = Uint8Array(buf).asByteArray()

                    response.headers.forEach {
                        if (it.key == "Set-Cookie" && request.cookieJar != null) {
                            request.cookieJar.add(HttpCookie(it.value))
                        } else {
                            response.headers[it.key] = it.value
                        }
                    }

                    response.networkTimeMs = Clock.System.now().toEpochMilliseconds() - timeStart
                    continuation.resume(response)

                    null
                }.catch { err ->
                    response.networkTimeMs = Clock.System.now().toEpochMilliseconds() - timeStart
                    continuation.resume(response)

                    null
                }
                null
            }.catch { err ->
                response.networkTimeMs = Clock.System.now().toEpochMilliseconds() - timeStart
                continuation.resume(response)

                null
            }
        } catch (e: Exception) {
            response.networkTimeMs = Clock.System.now().toEpochMilliseconds() - timeStart
            continuation.resume(response)
        }
    }
}

fun Uint8Array.asByteArray(): ByteArray = ByteArray(length) { this[it] }

fun toJsArrayImpl(vararg x: Byte): Uint8Array = js("new Uint8Array(x)")

fun ByteArray.asJsArray(): Uint8Array = toJsArrayImpl(*this)
