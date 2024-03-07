package net.k1ra.hoodies_network_kmm.mockwebserver.endpoints.httpbin

import net.k1ra.hoodies_network_kmm.mockwebserver.HttpCall
import net.k1ra.hoodies_network_kmm.mockwebserver.WebServerHandler


class Delete : WebServerHandler() {
    override suspend fun handleRequest(call: HttpCall) {
        delete {
            call.respond(200, HttpBinClone().handleRequest(call))
        }
    }

}