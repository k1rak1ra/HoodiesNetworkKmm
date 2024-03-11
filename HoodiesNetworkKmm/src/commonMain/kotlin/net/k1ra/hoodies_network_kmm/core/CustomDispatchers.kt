package net.k1ra.hoodies_network_kmm.core

import kotlinx.coroutines.CoroutineDispatcher

expect object CustomDispatchers {
    val IO: CoroutineDispatcher
}