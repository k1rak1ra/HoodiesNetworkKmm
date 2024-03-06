package net.k1ra.hoodies_network_kmm.core

import net.k1ra.hoodies_network_kmm.config.HttpClientConfig
import net.k1ra.hoodies_network_kmm.cookies.HttpCookie
import net.k1ra.hoodies_network_kmm.extensions.toByteArray
import net.k1ra.hoodies_network_kmm.extensions.toNsData
import net.k1ra.hoodies_network_kmm.request.NetworkRequest
import net.k1ra.hoodies_network_kmm.request.NetworkResponse
import kotlinx.datetime.Clock
import platform.Foundation.*
import platform.darwin.DISPATCH_TIME_FOREVER
import platform.darwin.dispatch_semaphore_create
import platform.darwin.dispatch_semaphore_signal
import platform.darwin.dispatch_semaphore_wait

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

        val semaphore = dispatch_semaphore_create(0)
        val components = NSURLComponents(request.url)

        val sessConfig = NSURLSessionConfiguration.defaultSessionConfiguration()
        sessConfig.waitsForConnectivity = true
        sessConfig.timeoutIntervalForResource = config.readTimeout.inWholeMilliseconds.toDouble()
        sessConfig.timeoutIntervalForRequest = config.connectTimeout.inWholeMilliseconds.toDouble()

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

        val task = session.dataTaskWithRequest(urlRequest) { nsData: NSData?, nsurlResponse: NSURLResponse?, nsError: NSError? ->
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

            dispatch_semaphore_signal(semaphore)
        }

        task.resume()
        session.finishTasksAndInvalidate()
        NSOperationQueue.mainQueue().waitUntilAllOperationsAreFinished()
        dispatch_semaphore_wait(semaphore, DISPATCH_TIME_FOREVER)

        response.networkTimeMs = Clock.System.now().toEpochMilliseconds() - timeStart

        return response
    }
}