package endpoints

import net.k1ra.hoodies_network_kmm.mockwebserver.HttpCall
import net.k1ra.hoodies_network_kmm.mockwebserver.WebServerHandler
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class CookieInspector : WebServerHandler() {
    override suspend fun handleRequest(call: HttpCall) {
        post {
            val response = mutableMapOf<String, JsonElement>()

            call.getHeaders()["Cookie"]?.split("; ")?.forEach { singleCookie ->
                val cookieParts = singleCookie.split("=")
                response[cookieParts[0]] = JsonPrimitive(cookieParts[1])
            }

            call.respond(200, JsonObject(response).toString())
        }
    }

}