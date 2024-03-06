package net.k1ra.hoodies_network_kmm.result

import net.k1ra.hoodies_network_kmm.request.NetworkRequest

data class HttpClientError(
    override val message: String?,
    var code : Int,
    val request: NetworkRequest? = null
    ) : Exception(message)
