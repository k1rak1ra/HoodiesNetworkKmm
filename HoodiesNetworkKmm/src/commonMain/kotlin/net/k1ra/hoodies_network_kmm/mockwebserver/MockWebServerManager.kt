package net.k1ra.hoodies_network_kmm.mockwebserver

/**
 * This class manages the MockWebServer
 */
class MockWebServerManager(private val builder: Builder) {
    private val serverSocket = ServerSocketFactory()

    /**
     * Starts the MockWebServer
     */
    fun start() = apply {
        serverSocket.createServerSocket(builder.port) {
            var foundMatch = false

            for (context in builder.contexts) {
                if (matchUrlAndExtractArgs(it, context.key)) {
                    context.value.internalHandler(it)
                    foundMatch = true
                }
            }

            //No contexts found to handle this path, send 404
            if (!foundMatch)
                it.respond(404, "No context found to handle the path ${it.path.split("?").first()}")
        }
    }

    private fun matchUrlAndExtractArgs(call: HttpCall, path: String) : Boolean {
        val pathSplit = path.split("/")
        val callPathSplit = call.path.split("?").first().split("/")

        var isMatch = true

        if (pathSplit.size == callPathSplit.size) {
            for (i in pathSplit.indices) {
                if (pathSplit[i].startsWith("{") && pathSplit[i].endsWith("}"))
                    call.callArguments[pathSplit[i].substring(1, pathSplit[i].length-1)] = callPathSplit[i]
                else if (pathSplit[i] != callPathSplit[i])
                    isMatch = false
            }
        } else {
            isMatch = false
        }

        return isMatch
    }

    /**
     * Stops the MockWebServer
     */
    fun stop() {
        serverSocket.stopServerSocket()
    }

    /**
     * Builder for the MockWebServer
     */
    class Builder {
        internal val contexts: HashMap<String, WebServerHandler> = HashMap()
        internal var port: Int = 6969

        /**
         * Called to add API endpoints to be served by the MockWebServer
         * For more details, see the WebServerHandler documentation
         */
        fun addContext(key: String, value: WebServerHandler) = apply {
            contexts[key] = value
        }

        /**
         * Specifies the port the MockWebServer should use. Default is 6969
         */
        fun usePort(port: Int) = apply {
            this.port = port
        }

        /**
         * Starts the MockWebServer and returns a MockWebServerManager object
         * To stop the MockWebServer, call the MockWebServerManager's stop() method
         */
        fun start() : MockWebServerManager {
            return MockWebServerManager(this).start()
        }
    }

}
