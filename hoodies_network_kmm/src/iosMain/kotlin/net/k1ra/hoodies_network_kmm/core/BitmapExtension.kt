package net.k1ra.hoodies_network_kmm.core

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import platform.Foundation.NSURLComponents
import platform.Foundation.NSURLQueryItem
import kotlin.system.getTimeMillis

actual object BitmapExtension {
    actual fun ByteArray.toImageBitmap(): ImageBitmap = Image.makeFromEncoded(this).toComposeImageBitmap()
}