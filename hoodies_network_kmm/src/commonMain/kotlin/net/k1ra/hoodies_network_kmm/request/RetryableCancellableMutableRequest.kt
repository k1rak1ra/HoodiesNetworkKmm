package net.k1ra.hoodies_network_kmm.request

class RetryableCancellableMutableRequest(
    request: NetworkRequest
) : CancellableMutableRequest(request) {
    internal var doRetry = false

    fun retryRequest() {
        doRetry = true
    }
}