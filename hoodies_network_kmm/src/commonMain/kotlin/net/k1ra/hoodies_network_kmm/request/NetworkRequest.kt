package net.k1ra.hoodies_network_kmm.request

import net.k1ra.hoodies_network_kmm.cookies.CookieJar
import net.k1ra.hoodies_network_kmm.core.InternalCallbacks

data class NetworkRequest(
    var method: String,
    var url: String,
    var headers: MutableMap<String, String>,
    var body: ByteArray?,
    val cookieJar: CookieJar?,
    internal val callbacks: InternalCallbacks
)