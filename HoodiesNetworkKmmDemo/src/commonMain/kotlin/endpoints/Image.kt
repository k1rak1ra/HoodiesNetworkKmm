package endpoints

import net.k1ra.hoodies_network_kmm.mockwebserver.HttpCall
import net.k1ra.hoodies_network_kmm.mockwebserver.WebServerHandler
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource


class Image : WebServerHandler() {
    @OptIn(ExperimentalResourceApi::class)
    override suspend fun handleRequest(call: HttpCall) {
        get {
            call.respond(200, resource("testimage.jpg").readBytes())
        }
    }

}