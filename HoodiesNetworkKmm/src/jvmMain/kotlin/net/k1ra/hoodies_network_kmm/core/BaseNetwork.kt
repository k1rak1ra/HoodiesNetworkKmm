package net.k1ra.hoodies_network_kmm.core

import net.k1ra.hoodies_network_kmm.config.HttpClientConfig
import net.k1ra.hoodies_network_kmm.cookies.HttpCookie
import net.k1ra.hoodies_network_kmm.request.NetworkRequest
import net.k1ra.hoodies_network_kmm.request.NetworkResponse
import kotlinx.datetime.Clock
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.URL

@Suppress("BlockingMethodInNonBlockingContext") //We're forcing this be only be called from coroutines by making it suspend, it's fine
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

        var conn: HttpURLConnection? = null

        try {
            conn = URL(request.url).openConnection() as HttpURLConnection
            conn.instanceFollowRedirects = HttpURLConnection.getFollowRedirects()
            conn.connectTimeout = config.connectTimeout.inWholeMilliseconds.toInt()
            conn.readTimeout = config.readTimeout.inWholeMilliseconds.toInt()
            conn.useCaches = false
            conn.doInput = true

            //Apply request headers
            request.headers.forEach {
                conn.setRequestProperty(it.key, it.value)
            }

            //Apply body data and set HTTP method
            conn.requestMethod = request.method
            if (request.body != null) {
                conn.doOutput = true
                conn.outputStream.write(request.body)
            }

            response.statusCode = conn.responseCode
            if (response.statusCode == -1) {
                /**
                 *  if the response code could not be retrieved -1 will be returned by getResponseCode().
                 * Signal to the caller that something was wrong with the connection.
                 * */

                throw IOException("Could not retrieve response code from HttpUrlConnection.")
            }

            val inputStream: InputStream? = if (response.statusCode < HttpURLConnection.HTTP_BAD_REQUEST) {
                conn.inputStream
            } else {
                // Error
                conn.errorStream
            }

            /** Add 0 byte response as a way of honestly representing a no-content request*/
            response.data = inputStream?.readBytes() ?: ByteArray(0)

            conn.headerFields.forEach {
                if (it.key != null && it.value != null) {
                    if (it.key == "Set-Cookie" && request.cookieJar != null) {
                        for (item in it.value)
                            request.cookieJar.add(HttpCookie(item))
                    } else {
                        response.headers[it.key] = it.value.first()
                    }
                }
            }

        } catch (e: SocketTimeoutException) {
            response.data = e.message?.encodeToByteArray() ?: response.data
            response.statusCode = -2
        } catch (e: MalformedURLException) {
            response.data = e.message?.encodeToByteArray() ?: response.data
            response.statusCode = -1
        } catch (e: IOException) {
            response.data = e.message?.encodeToByteArray() ?: response.data
            response.statusCode = -2
        } finally {
            conn?.disconnect()
        }

        response.networkTimeMs = Clock.System.now().toEpochMilliseconds() - timeStart

        return response
    }
}