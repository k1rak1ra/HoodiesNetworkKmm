package net.k1ra.hoodies_network_kmm.mockwebserver.endpoints

import net.k1ra.hoodies_network_kmm.mockwebserver.HttpCall
import net.k1ra.hoodies_network_kmm.mockwebserver.WebServerHandler

class WantsKeyHeader : WebServerHandler() {
    override suspend fun handleRequest(call: HttpCall) {
        get {
            val key = call.getHeaders()["key"]

            if (key != "20") {
                call.respond(401, "Unauthorized")
            } else {
                call.respond(200, "Success!")
            }
        }
    }

}