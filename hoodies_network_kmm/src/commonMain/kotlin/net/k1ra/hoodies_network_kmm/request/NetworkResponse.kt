package net.k1ra.hoodies_network_kmm.request

data class NetworkResponse(
    val request: NetworkRequest,
    var networkTimeMs: Long,
    var data: ByteArray,
    var statusCode: Int, //If exception thrown: -1 = generic error, -2 = retryable error, is HTTP status code otherwise
    var headers: MutableMap<String, String>
)