package net.k1ra.hoodies_network_kmm.cryptography

import korlibs.crypto.SecureRandom
import kotlinx.cinterop.*
import net.k1ra.hoodies_network_kmm.extensions.toByteArray
import net.k1ra.hoodies_network_kmm.extensions.toNsData
import net.k1ra.hoodies_network_kmm.util.Constants
import platform.CoreFoundation.CFAutorelease
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanFalse
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.kSecAttrAccessGroup
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.darwin.OSStatus
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
@OptIn(ExperimentalForeignApi::class)
actual object KeyManager {
    private const val serviceName = "net.k1ra.hoodies_network_kmm"
    private const val keyAlias = "HoodiesNetworkDbKey"

    private var key: ByteArray? = null

    actual fun getKey() : ByteArray {
        if (key != null)
            return key!!

        if (!doesKeyExist())
            generateNewKeyAndStore()

        key = getRaw()

        return key!!
    }

    private fun getRaw(): ByteArray? = context(keyAlias) { (account) ->
        val query = query(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to account,
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne,
        )

        memScoped {
            val result = alloc<CFTypeRefVar>()
            SecItemCopyMatching(query, result.ptr)
            CFBridgingRelease(result.value) as? NSData
        }
    }.let {
        it?.toByteArray()
    }

    private fun doesKeyExist() : Boolean  = context(keyAlias) { (account) ->
        val query = query(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to account,
            kSecReturnData to kCFBooleanFalse,
        )

        SecItemCopyMatching(query, null).validate()
    }

    private fun generateNewKeyAndStore() = context(keyAlias, generateNewKey().toNsData()) { (account, data) ->
        val query = query(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to account,
            kSecValueData to data
        )
        SecItemAdd(query, null).validate()
    }

    private fun generateNewKey() : ByteArray {
        val byteArray = ByteArray(Constants.AES_128_KEY_LENGTH)
        SecureRandom.nextBytes(byteArray)
        return byteArray
    }

    private fun <T> context(vararg values: Any?, block: Context.(List<CFTypeRef?>) -> T): T {
        val standard = mapOf(
            kSecAttrService to CFBridgingRetain(serviceName),
            kSecAttrAccessGroup to CFBridgingRetain(null)
        )
        val custom = arrayOf(*values).map { CFBridgingRetain(it) }
        return block.invoke(Context(standard), custom).apply {
            standard.values.plus(custom).forEach { CFBridgingRelease(it) }
        }
    }

    private fun OSStatus.validate(): Boolean {
        return (this.toUInt() == platform.darwin.noErr)
    }

    private class Context(val refs: Map<CFStringRef?, CFTypeRef?>) {
        fun query(vararg pairs: Pair<CFStringRef?, CFTypeRef?>): CFDictionaryRef? {
            val map = mapOf(*pairs).plus(refs.filter { it.value != null })
            return CFDictionaryCreateMutable(
                null, map.size.convert(), null, null
            ).apply {
                map.entries.forEach { CFDictionaryAddValue(this, it.key, it.value) }
            }.apply {
                CFAutorelease(this)
            }
        }
    }
}