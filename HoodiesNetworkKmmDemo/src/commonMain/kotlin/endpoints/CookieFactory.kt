package endpoints

import net.k1ra.hoodies_network_kmm.mockwebserver.HttpCall
import net.k1ra.hoodies_network_kmm.mockwebserver.WebServerHandler
import kotlinx.serialization.json.Json
import testObjects.CookieFactoryRequest

class CookieFactory : WebServerHandler() {
    private val kotlinJson = Json { ignoreUnknownKeys = true }

    override suspend fun handleRequest(call: HttpCall) {
        post {
            val body = kotlinJson.decodeFromString<List<CookieFactoryRequest>>(call.getBodyString())

            val respHeaders = mutableMapOf<String, List<String>>()
            respHeaders["Set-Cookie"] = body.map { "${it.name}=${it.value}; SameSite=Strict; HttpOnly" }

            call.setResponseHeaders(respHeaders)

            call.respond(200, ByteArray(0))
        }
    }

}