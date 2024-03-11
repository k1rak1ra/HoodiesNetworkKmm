package net.k1ra.hoodies_network_kmm.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual object CustomDispatchers {
    actual val IO: CoroutineDispatcher
        get() = Dispatchers.Default
}