package net.k1ra.hoodies_network_kmm.mockwebserver

internal actual class ServerSocketFactory {
    private var run = true

    actual fun stopServerSocket() {
        run = false
    }

    actual fun createServerSocket(port: Int,  callConsumer: suspend (HttpCall) -> Unit) {
        throw UnsupportedOperationException("Unfortunately, this is not supported on JS")
    }
}