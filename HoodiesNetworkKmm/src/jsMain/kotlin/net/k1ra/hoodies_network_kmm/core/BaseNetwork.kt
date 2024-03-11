package net.k1ra.hoodies_network_kmm.core

import kotlinx.browser.window
import kotlinx.datetime.Clock
import net.k1ra.hoodies_network_kmm.config.HttpClientConfig
import net.k1ra.hoodies_network_kmm.cookies.HttpCookie
import net.k1ra.hoodies_network_kmm.request.NetworkRequest
import net.k1ra.hoodies_network_kmm.request.NetworkResponse
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit

actual class BaseNetwork actual constructor() {
    actual suspend fun execRequest(request: NetworkRequest, config: HttpClientConfig): NetworkResponse {
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

        val w3cRequest = RequestInit(
            method = request.method,
            headers = requestHeaders,
            body = request.body,

        )

        window.fetch(request.url, w3cRequest).then {
            response.statusCode = it.status.toInt()
            response.data = it.body as ByteArray

            response.headers.forEach {
                if (it.key == "Set-Cookie" && request.cookieJar != null) {
                    request.cookieJar.add(HttpCookie(it.value))
                } else {
                    response.headers[it.key] = it.value
                }
            }
        }

        response.networkTimeMs = Clock.System.now().toEpochMilliseconds() - timeStart

        return response
    }
}