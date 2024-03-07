package net.k1ra.hoodies_network_kmm.testObjects

import kotlinx.serialization.Serializable

@Serializable
data class UrlQueryArgs(
    val test1: String? = null,
    val test2: String? = null,
    val test3: String? = null
)