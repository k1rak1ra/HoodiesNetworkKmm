@file:Suppress("UNCHECKED_CAST")

package net.k1ra.hoodies_network_kmm.mockwebserver.helper

import net.k1ra.hoodies_network_kmm.mockwebserver.HttpCall
import net.k1ra.hoodies_network_kmm.mockwebserver.MockWebServerManager
import net.k1ra.hoodies_network_kmm.mockwebserver.WebServerHandler
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


/**
 * Provides a friendly DSL for mocking APIs
 */
class MockServerMaker {
    class Builder {
        private var method = "POST"
        var expectedInput: Any = Unit
        private var expectedHeaders: Map<String, String> = hashMapOf()
        var successOutput = ""
        private var errorCode = 500
        private var errorMessage = "Expected input parameters were not received"
        var kotlinJson = Json { ignoreUnknownKeys = true }

        /**
         * Used to specify the accepted HTTP method (default is POST)
         * Requests with any other method will throw error 405
         */
        fun acceptMethod(method: String) = apply {
            this.method = method
        }

        /**
         * Used to specify the expected UrlEncodedParams for the page
         * If the parameters received by this page match the expected parameters, the success output will be returned
         * Otherwise, the error output will be returned
         */
        fun expect(urlParameters: Map<String, String>) = apply {
            expectedInput = urlParameters
        }

        /**
         * Used to specify the expected @Serializable object in the request body for this page
         * If the object received by this page match the expected object, the success output will be returned
         * Otherwise, the error output will be returned
         */
        inline fun <reified T> expect(serializable: T) = apply {
            expectedInput = kotlinJson.encodeToString(serializable)
        }

        /**
         * Used to specify the headers expected for this page
         * If the headers received by this page match the expected headers, the success output will be returned
         * Otherwise, the error output will be returned
         */
        fun expectHeaders(headers: Map<String, String>) = apply {
            expectedHeaders = headers
        }

        /**
         * This is the success output
         * This @Serializable object will be returned if the received input and headers match the expected input and headers
         */
        inline fun <reified T> returnThisObjectIfInputMatches(serializable: T) = apply {
            successOutput = kotlinJson.encodeToString(serializable)
        }

        /**
         * This is the error output
         * This HTTP code and message will be returned if the received input and headers do not match the expected input and headers
         */
        fun returnErrorIfInputDoesNotMatch(httpCode: Int, message: String) = apply {
            errorCode = httpCode
            errorMessage = message
        }

        /**
         * This method applies builds the mock and applied it to a MockWebServerManager Builder
         * @param path - the API endpoint to use. For example, if path = "/test", the page will be served at http://localhost:port/test
         * @param builder - The MockWebServerManager Builder that the mock will be served from
         */
        fun applyToMockWebServerBuilder(path: String, builder: MockWebServerManager.Builder) {
            val callConsumer: suspend (HttpCall) -> Unit = { call ->
                var paramsMatched: Boolean

                when (expectedInput) {
                    is Map<*, *> -> {
                        val input = call.getFormUrlEncodedParameters()
                        paramsMatched = true

                        for (item in expectedInput as HashMap<String, String>) {
                            paramsMatched = paramsMatched && item.value == input[item.key]
                        }
                    }
                    is Unit -> {
                        paramsMatched = call.getBodyByteArray().isEmpty()
                    }
                    else -> {
                        paramsMatched = expectedInput == call.getBodyString()
                    }
                }

                for (item in expectedHeaders) {
                    paramsMatched = paramsMatched && item.value == call.getHeaders()[item.key]
                }

                if (paramsMatched)
                    call.respond(200, successOutput)
                else
                    call.respond(errorCode, errorMessage)
            }

            val handler = object : WebServerHandler() {
                override suspend fun handleRequest(call: HttpCall) {
                    when (method) {
                        "GET" -> get{ callConsumer.invoke(call) }
                        "POST" -> post{ callConsumer.invoke(call) }
                        "PUT" -> put{ callConsumer.invoke(call) }
                        "DELETE" -> delete{ callConsumer.invoke(call) }
                        "PATCH" -> patch{ callConsumer.invoke(call) }
                    }
                }
            }

            builder.addContext(path, handler)
        }
    }
}
