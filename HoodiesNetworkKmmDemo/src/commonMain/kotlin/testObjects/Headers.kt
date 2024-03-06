package testObjects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Headers(
    @SerialName("Accept-Encoding") val acceptEncoding: String,
    @SerialName("User-Agent") val userAgent: String
)