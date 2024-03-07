package net.k1ra.hoodies_network_kmm.testObjects

import kotlinx.serialization.Serializable

@Serializable
class CookieFactoryRequest(
    val name: String,
    val value: String
)