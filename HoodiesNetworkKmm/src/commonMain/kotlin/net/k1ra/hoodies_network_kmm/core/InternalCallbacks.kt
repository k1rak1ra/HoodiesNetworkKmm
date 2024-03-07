package net.k1ra.hoodies_network_kmm.core

import net.k1ra.hoodies_network_kmm.request.NetworkResponse
import net.k1ra.hoodies_network_kmm.result.HttpClientError
import net.k1ra.hoodies_network_kmm.result.Result
import kotlinx.coroutines.CancellableContinuation

interface InternalCallbacks {
    fun successCallback(response: NetworkResponse, continuation: CancellableContinuation<Result<*>>, identifier: String)

    fun failureCallback(error: HttpClientError, continuation: CancellableContinuation<Result<*>>, identifier: String)
}