package net.k1ra.hoodies_network_kmm.cryptography


actual object KeyManager {
    actual fun getKey() : ByteArray {
        throw UnsupportedOperationException("Encrypted cache is not supported on web!")
    }
}