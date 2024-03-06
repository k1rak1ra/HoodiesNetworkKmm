package endpoints.httpbin

import net.k1ra.hoodies_network_kmm.mockwebserver.HttpCall
import net.k1ra.hoodies_network_kmm.mockwebserver.WebServerHandler


class Options : WebServerHandler() {
    override suspend fun handleRequest(call: HttpCall) {
        options {
            call.respond(200, HttpBinClone().handleRequest(call))
        }
    }

}