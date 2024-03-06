package net.k1ra.hoodies_network_kmm.request

import net.k1ra.hoodies_network_kmm.result.Result

open class CancellableMutableRequest(
    val request: NetworkRequest
) {
    internal var requestCancellationResult: Result<*>? = null

    fun cancelRequest(result: Result<*>) {
        requestCancellationResult = result
    }
}