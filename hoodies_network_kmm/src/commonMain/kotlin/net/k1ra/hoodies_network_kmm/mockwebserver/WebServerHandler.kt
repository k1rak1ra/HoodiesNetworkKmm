package net.k1ra.hoodies_network_kmm.mockwebserver


/**
 * Base class for handling requests in the MockWebServer
 * For every endpoint being served by the MockWebServer, a class inheriting from WebServerHandler must be made
 * Then, the class needs to override handleRequest(call: HttpCall) in order to handle the request using a ktor-like syntax
 */
open class WebServerHandler {
    private var postRunnable: (suspend () -> Unit)? = null
    private var getRunnable: (suspend () -> Unit)? = null
    private var putRunnable: (suspend () -> Unit)? = null
    private var deleteRunnable: (suspend () -> Unit)? = null
    private var optionsRunnable: (suspend () -> Unit)? = null
    private var patchRunnable: (suspend () -> Unit)? = null

    internal suspend fun internalHandler(call: HttpCall) {
        handleRequest(call)

        when(call.method) {
            "POST" -> postRunnable?.invoke() ?: return405(call)
            "GET" -> getRunnable?.invoke() ?: return405(call)
            "PUT" -> putRunnable?.invoke() ?: return405(call)
            "DELETE" -> deleteRunnable?.invoke() ?: return405(call)
            "OPTIONS" -> optionsRunnable?.invoke() ?: return405(call)
            "PATCH" -> patchRunnable?.invoke() ?: return405(call)
        }

        //If call hasn't been responded to, send empty 200
        if (!call.responded)
            call.respond(200, ByteArray(0))
    }

    private suspend fun return405(call: HttpCall) {
        call.respond(405,"Method not allowed" )
    }

    /**
     * This class needs to be overridden with your logic
     */
    open suspend fun handleRequest(call: HttpCall) {
        //Left open for overriding
    }

    fun post(runnable: suspend () -> Unit) {
        postRunnable = runnable
    }

    fun get(runnable: suspend () -> Unit) {
        getRunnable = runnable
    }

    fun put(runnable: suspend () -> Unit) {
        putRunnable = runnable
    }

    fun delete(runnable: suspend () -> Unit) {
        deleteRunnable = runnable
    }

    fun options(runnable: suspend () -> Unit) {
        optionsRunnable = runnable
    }

    fun patch(runnable: suspend () -> Unit) {
        patchRunnable = runnable
    }
}