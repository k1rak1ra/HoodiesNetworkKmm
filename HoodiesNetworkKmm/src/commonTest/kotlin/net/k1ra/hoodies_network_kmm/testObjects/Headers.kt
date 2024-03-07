package net.k1ra.hoodies_network_kmm.testObjects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Headers(
    @SerialName("User-Agent") val userAgent: String
)