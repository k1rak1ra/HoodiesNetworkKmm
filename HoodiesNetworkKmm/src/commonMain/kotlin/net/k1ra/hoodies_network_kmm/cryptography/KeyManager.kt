package net.k1ra.hoodies_network_kmm.cryptography

expect object KeyManager {
    fun getKey() : ByteArray
}