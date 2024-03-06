package net.k1ra.hoodies_network_kmm.mockwebserver

import net.k1ra.hoodies_network_kmm.core.FormUrlEncoder

/**
 * Provides a friendly interface for interacting with HTTP requests and sending responses
 */
class HttpCall(
    internal val method: String,
    val path: String,
    internal val headers: Map<String, String>,
    private val body: ByteArray,
    private val callback: suspend (ByteArray) -> Unit
) {
    private var responseHeaders = mapOf<String, List<String>>()
    internal var callArguments = mutableMapOf<String, String>()
    internal var responded = false

    /**
     * Returns the request's body as a ByteArray
     */
    fun getBodyByteArray() : ByteArray {
        return body
    }

    /**
     * Returns the request's body as a String
     */
    fun getBodyString() : String {
        return getBodyByteArray().decodeToString()
    }

    /**
     * Returns call arguments specified with {} wildcards in the URL
     */
    fun getCallArguments() : Map<String, String> {
        return callArguments
    }

    /**
     * Gets FormUrlEncoded parameters from the GET/POST request's URL
     */
    fun getFormUrlEncodedParameters() : Map<String, String> {
        var map = hashMapOf<String, String>()

        if (path.split("?").size != 2)
            return map

        val queryString = path.split("?")[1]

        //take the String with POST params and turn it into a HashMap
        map = FormUrlEncoder.decode(queryString) as HashMap<String, String>

        return map
    }

    /**
     * Returns the request headers
     */
    fun getHeaders() : Map<String, String> {
        return headers
    }

    /**
     * Used to set the response headers
     */
    fun setResponseHeaders(headers: Map<String, List<String>>) {
        responseHeaders = headers
    }

    /**
     * Used to respond with an HTTP status code and response String
     */
    suspend fun respond(code: Int, response: String) {
        respond(code, response.encodeToByteArray())
    }

    /**
     * Used to respond with an HTTP status code and response ByteArray
     */
    suspend fun respond(code: Int, response: ByteArray) {
        val respBuilder = StringBuilder()

        respBuilder.append("HTTP/1.1 $code\r\n")
        respBuilder.append("Content-Type: application/json;charset-UTF-8\r\n")
        respBuilder.append("Server: HoodiesNetworkKmmMockWebServer\r\n")

        for (header in responseHeaders)
            for (value in header.value)
                respBuilder.append("${header.key}: ${value}\r\n")

        respBuilder.append("Content-Length: ${response.size}\r\n")
        respBuilder.append("\r\n")

        responded = true
        callback.invoke(respBuilder.toString().encodeToByteArray() + response)
    }
}