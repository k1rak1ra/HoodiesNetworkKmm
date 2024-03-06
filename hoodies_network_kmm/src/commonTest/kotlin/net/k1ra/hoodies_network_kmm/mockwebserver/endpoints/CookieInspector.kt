package net.k1ra.hoodies_network_kmm.mockwebserver.endpoints

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.k1ra.hoodies_network_kmm.mockwebserver.HttpCall
import net.k1ra.hoodies_network_kmm.mockwebserver.WebServerHandler
import net.k1ra.hoodies_network_kmm.testObjects.CookieFactoryRequest

class CookieInspector : WebServerHandler() {
    private val kotlinJson = Json { ignoreUnknownKeys = true }

    override suspend fun handleRequest(call: HttpCall) {
        post {
            val response = arrayListOf<CookieFactoryRequest>()

            call.getHeaders()["Cookie"]?.split("; ")?.forEach { singleCookie ->
                val cookieParts = singleCookie.split("=")

                if (cookieParts.size > 1)
                response.add(CookieFactoryRequest(cookieParts[0], cookieParts[1]))
            }

            call.respond(200, kotlinJson.encodeToString(response))
        }
    }

}