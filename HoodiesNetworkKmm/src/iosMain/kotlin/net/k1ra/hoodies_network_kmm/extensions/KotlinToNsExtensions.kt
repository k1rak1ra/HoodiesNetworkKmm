package net.k1ra.hoodies_network_kmm.extensions

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray = ByteArray(this@toByteArray.length.toInt()).apply {
    usePinned {
        memcpy(it.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun ByteArray.toNsData() : NSData = memScoped {
    NSData.create(
        bytes = allocArrayOf(this@toNsData),
        length = this@toNsData.size.toULong()
    )
}