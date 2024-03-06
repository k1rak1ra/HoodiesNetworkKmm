package net.k1ra.hoodies_network_kmm.cryptography

import androidx.security.crypto.EncryptedSharedPreferences
import korlibs.crypto.SecureRandom
import korlibs.encoding.fromBase64
import korlibs.encoding.toBase64
import net.k1ra.hoodies_network_kmm.HoodiesNetworkInit
import net.k1ra.hoodies_network_kmm.config.HttpClientConfig
import net.k1ra.hoodies_network_kmm.util.Constants


actual object KeyManager {
    private const val keyAlias = "HoodiesNetworkDbKey"
    private var key: ByteArray? = null

    actual fun getKey() : ByteArray {
        if (key != null)
            return key!!

        //If we're in test mode and can't use EncryptedSharedPref, generate and use a temp key just for this run
        if (HttpClientConfig.testMode) {
            key = generateNewKey()
            return key!!
        }

        HoodiesNetworkInit.context ?: HoodiesNetworkInit.throwExceptionForMissingContext()

        val sharedPreferences = EncryptedSharedPreferences.create(
            keyAlias,
            keyAlias,
            HoodiesNetworkInit.context!!,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        if (sharedPreferences.getString(keyAlias, null) == null) {
            //Generate and store new key
            val editor = sharedPreferences.edit()
            editor.putString(keyAlias, generateNewKey().toBase64())
            editor.apply()
        }

        key = sharedPreferences.getString(keyAlias, null)!!.fromBase64()
        return key!!
    }

    private fun generateNewKey() : ByteArray {
        val byteArray = ByteArray(Constants.AES_128_KEY_LENGTH)
        SecureRandom.nextBytes(byteArray)
        return byteArray
    }
}