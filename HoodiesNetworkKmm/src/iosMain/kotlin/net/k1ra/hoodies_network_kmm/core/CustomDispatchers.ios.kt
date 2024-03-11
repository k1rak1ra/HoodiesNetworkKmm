package net.k1ra.hoodies_network_kmm.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.newFixedThreadPoolContext

actual object CustomDispatchers {
    actual val IO: CoroutineDispatcher
        get() = newFixedThreadPoolContext(nThreads = 200, name = "IO")
}