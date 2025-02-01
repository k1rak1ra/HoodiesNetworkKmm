package net.k1ra.hoodies_network_kmm.interceptor

import net.k1ra.hoodies_network_kmm.request.CancellableMutableRequest
import net.k1ra.hoodies_network_kmm.request.NetworkResponse
import net.k1ra.hoodies_network_kmm.request.RetryableCancellableMutableRequest
import net.k1ra.hoodies_network_kmm.result.HttpClientError
import net.k1ra.hoodies_network_kmm.result.Result


open class Interceptor {

    open suspend fun interceptRequest(identifier: String, cancellableMutableRequest: CancellableMutableRequest) {
        //Stub
    }

    open suspend fun interceptError(error: HttpClientError, retryableCancellableMutableRequest: RetryableCancellableMutableRequest, autoRetryAttempts: Int) {
        //Stub
    }

    open suspend fun interceptResponse(result: Result<*>, response: NetworkResponse) {
        //Stub
    }

}