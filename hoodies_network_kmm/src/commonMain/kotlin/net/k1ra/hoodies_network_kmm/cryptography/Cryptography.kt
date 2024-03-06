package net.k1ra.hoodies_network_kmm.cryptography

import korlibs.crypto.AES
import korlibs.crypto.CipherPadding
import korlibs.crypto.SecureRandom
import net.k1ra.hoodies_network_kmm.util.Constants

object Cryptography {
    fun runAes(input: ByteArray, iv: ByteArray, cipherMode: CipherMode) : ByteArray {
        return when(cipherMode) {
            CipherMode.ENCRYPT -> AES.encryptAesCbc(input, KeyManager.getKey(), iv, CipherPadding.PKCS7Padding)
            CipherMode.DECRYPT -> AES.decryptAesCbc(input, KeyManager.getKey(), iv, CipherPadding.PKCS7Padding)
        }
    }

    fun generateIv() : ByteArray {
        val ivBytes = ByteArray(Constants.AES_128_IV_LENGTH)
        SecureRandom.nextBytes(ivBytes)

        return ivBytes
    }
}