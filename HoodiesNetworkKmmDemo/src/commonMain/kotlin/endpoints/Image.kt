package endpoints

import net.k1ra.hoodies_network_kmm.mockwebserver.HttpCall
import net.k1ra.hoodies_network_kmm.mockwebserver.WebServerHandler
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.readResourceBytes


class Image : WebServerHandler() {
    @OptIn(InternalResourceApi::class)
    override suspend fun handleRequest(call: HttpCall) {
        get {
            call.respond(200, readResourceBytes("files/testimage.jpg"))
        }
    }

}