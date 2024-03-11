package net.k1ra.hoodies_network_kmm.cryptography

import korlibs.crypto.SecureRandom
import net.k1ra.hoodies_network_kmm.util.Constants


actual object KeyManager {
    private var key: ByteArray? = null

    actual fun getKey() : ByteArray {
        if (key != null)
            return key!!

        key = generateNewKey()
        return key!!
    }

    private fun generateNewKey() : ByteArray {
        val byteArray = ByteArray(Constants.AES_128_KEY_LENGTH)
        SecureRandom.nextBytes(byteArray)
        return byteArray
    }
}