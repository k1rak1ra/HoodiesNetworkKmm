@file:Suppress("UNCHECKED_CAST")

package net.k1ra.hoodies_network_kmm

import androidx.compose.ui.graphics.ImageBitmap
import net.k1ra.hoodies_network_kmm.cache.OptionallyEncryptedCache
import net.k1ra.hoodies_network_kmm.cache.configuration.CacheConfiguration
import net.k1ra.hoodies_network_kmm.cache.configuration.CacheDisabled
import net.k1ra.hoodies_network_kmm.cache.configuration.CacheEnabled
import net.k1ra.hoodies_network_kmm.config.HttpClientConfig
import net.k1ra.hoodies_network_kmm.cookies.CookieJar
import net.k1ra.hoodies_network_kmm.core.BaseNetwork
import net.k1ra.hoodies_network_kmm.core.InternalCallbacks
import net.k1ra.hoodies_network_kmm.core.BitmapExtension.toImageBitmap
import net.k1ra.hoodies_network_kmm.interceptor.Interceptor
import net.k1ra.hoodies_network_kmm.request.CancellableMutableRequest
import net.k1ra.hoodies_network_kmm.request.NetworkRequest
import net.k1ra.hoodies_network_kmm.request.NetworkResponse
import net.k1ra.hoodies_network_kmm.request.RetryableCancellableMutableRequest
import net.k1ra.hoodies_network_kmm.result.Failure
import net.k1ra.hoodies_network_kmm.result.HttpClientError
import net.k1ra.hoodies_network_kmm.result.Success
import net.k1ra.hoodies_network_kmm.result.Result
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.k1ra.hoodies_network_kmm.core.FormUrlEncoder
import kotlin.coroutines.resume
import kotlin.time.Duration
import kotlin.reflect.typeOf

class HoodiesNetworkClient(val builder: Builder) {
    private val retryAttempts = LinkedHashMap<String, Int>()

    class Builder {
        var baseUrl = ""
        var maxRetryLimit = 3
        var defaultHeaders: Map<String, String> = hashMapOf()
        var retryOnConnectionFailure = true
        var kotlinJson = Json { ignoreUnknownKeys = true }
        var retryDelayDuration = Duration.ZERO
        var interceptors: List<Interceptor> = listOf()
        var config = HttpClientConfig()
        var cacheConfiguration: CacheConfiguration = CacheDisabled()
        var cookieJar: CookieJar? = null

        fun build() = HoodiesNetworkClient(this)
    }

    suspend inline fun <reified T, reified B> post(
        endpoint: String,
        body: B? = null,
        extraHeaders: Map<String, String>? = null,
        customCache: CacheConfiguration? = null) : Result<T> {
        return spawnContinuationAndExec(buildRequest<T, B>("POST", endpoint, body, extraHeaders), customCache ?: builder.cacheConfiguration) as Result<T>
    }

    suspend inline fun <reified T> post(
        endpoint: String,
        urlParams: Map<String, String>? = null,
        extraHeaders: Map<String, String>? = null,
        customCache: CacheConfiguration? = null) : Result<T> {
        return spawnContinuationAndExec(buildRequestWithUrlQueryParams<T>("POST", endpoint, urlParams, extraHeaders), customCache ?: builder.cacheConfiguration) as Result<T>
    }

    suspend inline fun <reified T> get(
        endpoint: String,
        urlParams: Map<String, String>? = null,
        extraHeaders: Map<String, String>? = null,
        customCache: CacheConfiguration? = null) : Result<T> {
        return spawnContinuationAndExec(buildRequestWithUrlQueryParams<T>("GET", endpoint, urlParams, extraHeaders), customCache ?: builder.cacheConfiguration) as Result<T>
    }

    suspend inline fun <reified T, reified B> patch(
        endpoint: String,
        body: B? = null,
        extraHeaders: Map<String, String>? = null,
        customCache: CacheConfiguration? = null) : Result<T> {
        return spawnContinuationAndExec(buildRequest<T, B>("PATCH", endpoint, body, extraHeaders), customCache ?: builder.cacheConfiguration) as Result<T>
    }

    suspend inline fun <reified T> patch(
        endpoint: String,
        urlParams: Map<String, String>? = null,
        extraHeaders: Map<String, String>? = null,
        customCache: CacheConfiguration? = null) : Result<T> {
        return spawnContinuationAndExec(buildRequestWithUrlQueryParams<T>("PATCH", endpoint, urlParams, extraHeaders), customCache ?: builder.cacheConfiguration) as Result<T>
    }

    suspend inline fun <reified T, reified B> put(
        endpoint: String,
        body: B? = null,
        extraHeaders: Map<String, String>? = null,
        customCache: CacheConfiguration? = null) : Result<T> {
        return spawnContinuationAndExec(buildRequest<T, B>("PUT", endpoint, body, extraHeaders), customCache ?: builder.cacheConfiguration) as Result<T>
    }

    suspend inline fun <reified T, reified B> delete(
        endpoint: String,
        body: B? = null,
        extraHeaders: Map<String, String>? = null,
        customCache: CacheConfiguration? = null) : Result<T> {
        return spawnContinuationAndExec(buildRequest<T, B>("DELETE", endpoint, body, extraHeaders), customCache ?: builder.cacheConfiguration) as Result<T>
    }

    suspend inline fun <reified T> delete(
        endpoint: String,
        extraHeaders: Map<String, String>? = null,
        customCache: CacheConfiguration? = null) : Result<T> {
        return spawnContinuationAndExec(buildRequest<T, Unit>("DELETE", endpoint, null, extraHeaders), customCache ?: builder.cacheConfiguration) as Result<T>
    }

    inline fun <reified T, reified B> buildRequest(type: String, endpoint: String, body: B? = null, extraHeaders: Map<String, String>? = null) : NetworkRequest {
        val headers = mutableMapOf("Content-Type" to "application/json")
        headers.putAll(builder.defaultHeaders)
        if (extraHeaders != null)
            headers.putAll(extraHeaders)

        return NetworkRequest(
            type,
            buildPathQuery(builder.baseUrl, endpoint),
            headers,
            convertRequestBody(body),
            builder.cookieJar,
            buildDefaultCallback<T>()
        )
    }

    inline fun <reified T> buildRequestWithUrlQueryParams(type: String, endpoint: String, urlParams: Map<String, String>? = null, extraHeaders: Map<String, String>? = null) : NetworkRequest {
        val headers = mutableMapOf("Content-Type" to "application/x-www-form-urlencoded")
        headers.putAll(builder.defaultHeaders)
        if (extraHeaders != null)
            headers.putAll(extraHeaders)

        val urlBuilder = StringBuilder()
        urlBuilder.append(buildPathQuery(builder.baseUrl, endpoint))
        if (urlParams?.isNotEmpty() == true)
            urlBuilder.append("?${FormUrlEncoder.encode(urlParams)}")

        return NetworkRequest(
            type,
            urlBuilder.toString(),
            headers,
            null,
            builder.cookieJar,
            buildDefaultCallback<T>()
        )
    }

    inline fun <reified T> buildDefaultCallback() : InternalCallbacks {
        return object : InternalCallbacks {
            override fun successCallback(response: NetworkResponse, continuation: CancellableContinuation<Result<*>>, identifier: String) {
                val result = Success(convertResponseBody<T>(response.data), response)

                for (interceptor in builder.interceptors)
                    interceptor.interceptResponse(result)

                completeRequestWithResult(continuation, result, identifier)
            }

            override fun failureCallback(error: HttpClientError, continuation: CancellableContinuation<Result<*>>, identifier: String) {
                completeRequestWithResult(continuation, Failure(error), identifier)
            }

        }
    }

    inline fun <reified B> convertRequestBody(body: B? = null) : ByteArray {
        return when (body) {
            null -> ByteArray(0)
            is Unit -> ByteArray(0)
            is ByteArray -> body
            is String -> "\"$body\"".encodeToByteArray()
            else -> builder.kotlinJson.encodeToString(body).encodeToByteArray()
        }
    }

    inline fun <reified T> convertResponseBody(data: ByteArray) : T {
        return when (typeOf<T>()) {
            typeOf<Unit>() -> Unit as T
            typeOf<ByteArray>() -> data as T
            typeOf<String>() -> data.decodeToString() as T
            typeOf<ImageBitmap>() -> data.toImageBitmap() as T
            else -> builder.kotlinJson.decodeFromString(data.decodeToString())
        }
    }

    suspend fun spawnContinuationAndExec(request: NetworkRequest, cache: CacheConfiguration) : Result<*> = suspendCancellableCoroutine { continuation ->
        CoroutineScope(Dispatchers.IO).launch {
            val identifier = Clock.System.now().toEpochMilliseconds().toString()
            retryAttempts[identifier] = 0

            execRequest(request, continuation, identifier, cache)
        }
    }

    private suspend fun execRequest(
        request: NetworkRequest,
        continuation: CancellableContinuation<Result<*>>,
        identifier: String,
        cacheConfig: CacheConfiguration
    ) = CoroutineScope(Dispatchers.IO).launch {
        //Intercept request and execute cancellation if requested
        for (interceptor in builder.interceptors) {
            val cancellableMutableRequest = CancellableMutableRequest(request)
            interceptor.interceptRequest(identifier, cancellableMutableRequest)

            if (cancellableMutableRequest.requestCancellationResult != null) {
                completeRequestWithResult(continuation, cancellableMutableRequest.requestCancellationResult!!, identifier)
                return@launch
            }
        }

        val cache: OptionallyEncryptedCache? = if (cacheConfig is CacheEnabled) {
            OptionallyEncryptedCache(cacheConfig)
        } else {
            null
        }

        if (request.cookieJar != null && request.cookieJar.get().size > 0) {
            val cookieHeaderBuiler = StringBuilder()

            for (cookie in request.cookieJar.get())
                cookieHeaderBuiler.append("${cookie.name}=${cookie.value}; ")

            request.headers["Cookie"] = cookieHeaderBuiler.toString()
        }

        val response = if (cache?.isDataStale(request) == false) {
            cache.getCachedData(request)
        } else {
            BaseNetwork().execRequest(request, builder.config)
        }

        if (response.statusCode < 200 || response.statusCode > 209) {
            //Request failed, if error type is un-retryable, if retry is disabled, or if we've run out of retry attempts, give up and call failure callback
            if (response.statusCode == -1 || !builder.retryOnConnectionFailure ||
                (builder.retryOnConnectionFailure && (retryAttempts[identifier] ?: builder.maxRetryLimit) >= builder.maxRetryLimit)
            ) {
                val error = HttpClientError(response.data.decodeToString(), response.statusCode, request)

                for (interceptor in builder.interceptors) {
                    val retryableRequest = RetryableCancellableMutableRequest(request)
                    interceptor.interceptError(error, retryableRequest, retryAttempts[identifier] ?: builder.maxRetryLimit)

                    if (retryableRequest.requestCancellationResult != null) {
                        completeRequestWithResult(continuation, retryableRequest.requestCancellationResult!!, identifier)
                        return@launch
                    } else if (retryableRequest.doRetry) {
                        waitAndRetry(retryableRequest.request, continuation, identifier, cacheConfig)
                        return@launch
                    }
                }

                request.callbacks.failureCallback(error, continuation, identifier)
            } else {
                waitAndRetry(request, continuation, identifier, cacheConfig)
            }
        } else {
            //Request succeeded
            //If response time is 0, we know cache fetch happened, so do not cache the result
            //Also don't cache an empty result
            if (response.networkTimeMs != 0L && response.data.isNotEmpty())
                cache?.cacheRequestResult(response.data, request)

            request.callbacks.successCallback(response, continuation, identifier)
        }
    }

    private suspend fun waitAndRetry(request: NetworkRequest, continuation: CancellableContinuation<Result<*>>, identifier: String, cache: CacheConfiguration) {
        delay(builder.retryDelayDuration.inWholeMilliseconds)
        retryAttempts[identifier] = retryAttempts[identifier]!!.plus(1)
        execRequest(request, continuation, identifier, cache)
    }

    fun completeRequestWithResult(continuation: CancellableContinuation<Result<*>>, result: Result<*>, identifier: String) {
        retryAttempts.remove(identifier)

        if (continuation.isCancelled || continuation.isCompleted)
            return

        continuation.resume(result)
    }

    private fun normalizeTailingSlashOnBaseUrl(url: String) : String {
        return if (url.endsWith("/") || url.isEmpty())
            url
        else
            "$url/"
    }

    private fun normalizeLeadingSlashOnEndpoint(endpoint: String) : String {
        return if (endpoint.startsWith("/"))
            endpoint.substring(1, endpoint.length)
        else
            endpoint
    }

    fun buildPathQuery(url: String, endpoint: String) : String {
        return "${normalizeTailingSlashOnBaseUrl(url)}${normalizeLeadingSlashOnEndpoint(endpoint)}"
    }
}