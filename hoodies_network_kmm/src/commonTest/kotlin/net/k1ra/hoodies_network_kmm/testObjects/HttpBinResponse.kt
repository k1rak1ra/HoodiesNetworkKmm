package net.k1ra.hoodies_network_kmm.testObjects

import kotlinx.serialization.Serializable

@Serializable
data class HttpBinResponse(
    val headers: Headers,
    val data: String,
    val url: String,
    val args: UrlQueryArgs?
)