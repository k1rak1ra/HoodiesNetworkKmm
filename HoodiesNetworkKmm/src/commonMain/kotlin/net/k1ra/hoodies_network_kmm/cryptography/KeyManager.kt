package net.k1ra.hoodies_network_kmm.cryptography

import korlibs.crypto.SecureRandom
import net.k1ra.sharedprefkmm.SharedPreferences
import net.k1ra.sharedprefkmm.util.Constants

internal object KeyManager {
    private var key: ByteArray? = null
    private val prefs = SharedPreferences("HoodiesNetworkCacheKey")

    suspend fun getKey() : ByteArray {
        if (key != null)
            return key as ByteArray

        key = prefs.get<ByteArray>("HoodiesNetworkCacheKey")

        if (key == null) {
            key = generateNewKey()
            prefs.set("HoodiesNetworkCacheKey", key)
        }

        return key as ByteArray
    }

    private fun generateNewKey() : ByteArray {
        val byteArray = ByteArray(Constants.AES_256_KEY_LENGTH)
        SecureRandom.nextBytes(byteArray)
        return byteArray
    }
}