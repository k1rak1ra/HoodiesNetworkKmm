package net.k1ra.hoodies_network_kmm.mockwebserver.endpoints.httpbin

import net.k1ra.hoodies_network_kmm.mockwebserver.HttpCall
import net.k1ra.hoodies_network_kmm.mockwebserver.WebServerHandler
import net.k1ra.hoodies_network_kmm.mockwebserver.endpoints.httpbin.HttpBinClone


class Post : WebServerHandler() {
    override suspend fun handleRequest(call: HttpCall) {
        post {
            call.respond(200, HttpBinClone().handleRequest(call))
        }
    }

}