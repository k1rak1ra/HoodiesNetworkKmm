package net.k1ra.hoodies_network_kmm.mockwebserver.endpoints.httpbin

import net.k1ra.hoodies_network_kmm.mockwebserver.HttpCall
import net.k1ra.hoodies_network_kmm.mockwebserver.WebServerHandler


class Put : WebServerHandler() {
    override suspend fun handleRequest(call: HttpCall) {
        put {
            call.respond(200, HttpBinClone().handleRequest(call))
        }
    }

}