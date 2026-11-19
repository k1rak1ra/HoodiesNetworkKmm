package net.k1ra.hoodies_network_kmm.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import net.k1ra.hoodies_network_kmm.config.HttpClientConfig
import net.k1ra.hoodies_network_kmm.cookies.HttpCookie
import net.k1ra.hoodies_network_kmm.extensions.toByteArray
import net.k1ra.hoodies_network_kmm.extensions.toNsData
import net.k1ra.hoodies_network_kmm.request.NetworkRequest
import net.k1ra.hoodies_network_kmm.request.NetworkResponse
import platform.Foundation.HTTPBody
import platform.Foundation.HTTPMethod
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURLComponents
import platform.Foundation.NSURLErrorDomain
import platform.Foundation.NSURLErrorNotConnectedToInternet
import platform.Foundation.NSURLResponse
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionConfiguration
import platform.Foundation.allHTTPHeaderFields
import platform.Foundation.dataTaskWithRequest
import kotlin.coroutines.resume
import kotlin.time.Clock

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

        val components = NSURLComponents(request.url)

        val sessConfig = NSURLSessionConfiguration.defaultSessionConfiguration()
        sessConfig.timeoutIntervalForResource = config.readTimeout.inWholeSeconds.toDouble()
        sessConfig.timeoutIntervalForRequest = config.connectTimeout.inWholeSeconds.toDouble()

        //Set HTTP method
        val urlRequest = NSMutableURLRequest(components.URL!!)
        urlRequest.HTTPMethod = request.method

        //Apply request headers
        urlRequest.allHTTPHeaderFields = request.headers.toMap()

        //Apply body data
        if (request.body != null) {
            urlRequest.HTTPBody = request.body!!.toNsData()
        }

        val session = NSURLSession.sessionWithConfiguration(
            sessConfig, null, NSOperationQueue.mainQueue()
        )

        session.dataTaskWithRequest(urlRequest) { nsData: NSData?, nsurlResponse: NSURLResponse?, nsError: NSError? ->
            CoroutineScope(Dispatchers.IO).launch {
                nsurlResponse?.let {
                    response.statusCode = (it as NSHTTPURLResponse).statusCode.toInt()

                    it.allHeaderFields.forEach { header ->
                        if (header.key is String && header.value is String) {
                            if (header.key == "Set-Cookie" && request.cookieJar != null) {
                                for (line in (header.value as String).split(","))
                                    request.cookieJar.add(HttpCookie(line))
                            } else {
                                response.headers[header.key as String] = header.value as String //These cases shouldn't be necessary but compiler complains if they're missing...
                            }
                        }
                    }
                }

                if (nsData?.length()?.toInt() != 0)
                    nsData?.let { response.data = it.toByteArray() }

                if (nsError != null) {
                    if (nsError.domain == NSURLErrorDomain && nsError.code == NSURLErrorNotConnectedToInternet) {
                        response.statusCode = -2
                    } else {
                        response.statusCode = -1
                    }

                    response.data = nsError.localizedDescription.encodeToByteArray()
                }


                session.finishTasksAndInvalidate()
                response.networkTimeMs = Clock.System.now().toEpochMilliseconds() - timeStart

                continuation.resume(response)
            }
        }.resume()
    }
}