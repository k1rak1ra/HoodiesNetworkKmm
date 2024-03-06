package testObjects

import kotlinx.serialization.Serializable

@Serializable
data class HttpBinResponse(
    val headers: Headers,
    val data: String,
    val url: String
)