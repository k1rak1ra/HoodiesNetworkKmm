package net.k1ra.hoodies_network_kmm.mockwebserver.endpoints

import net.k1ra.hoodies_network_kmm.mockwebserver.HttpCall
import net.k1ra.hoodies_network_kmm.mockwebserver.WebServerHandler
import kotlinx.coroutines.delay


class EchoDelay : WebServerHandler() {
    override suspend fun handleRequest(call: HttpCall) {
        get {
            val delayLength = call.getCallArguments()["length"]!!

            delay(delayLength.toLong() * 1000L)

            call.respond(200, "{\"delay\":\"$delayLength\"}")
        }
    }

}