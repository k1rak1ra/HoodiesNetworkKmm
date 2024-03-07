package net.k1ra.hoodies_network_kmm.core

import androidx.compose.ui.graphics.ImageBitmap

expect object BitmapExtension {
    fun ByteArray.toImageBitmap(): ImageBitmap
}