package net.k1ra.hoodies_network_kmm.mockwebserver

internal expect class ServerSocketFactory() {
    fun stopServerSocket()

    fun createServerSocket(port: Int, callConsumer: suspend (HttpCall) -> Unit)
}