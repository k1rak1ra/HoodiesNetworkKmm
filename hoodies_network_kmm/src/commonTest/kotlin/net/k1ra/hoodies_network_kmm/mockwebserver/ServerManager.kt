package net.k1ra.hoodies_network_kmm.mockwebserver

import net.k1ra.hoodies_network_kmm.mockwebserver.endpoints.CookieFactory
import net.k1ra.hoodies_network_kmm.mockwebserver.endpoints.CookieInspector
import net.k1ra.hoodies_network_kmm.mockwebserver.endpoints.EchoDelay
import net.k1ra.hoodies_network_kmm.mockwebserver.endpoints.Image
import net.k1ra.hoodies_network_kmm.mockwebserver.endpoints.WantsKeyHeader
import net.k1ra.hoodies_network_kmm.mockwebserver.endpoints.httpbin.Delete
import net.k1ra.hoodies_network_kmm.mockwebserver.endpoints.httpbin.Get
import net.k1ra.hoodies_network_kmm.mockwebserver.endpoints.httpbin.Options
import net.k1ra.hoodies_network_kmm.mockwebserver.endpoints.httpbin.Patch
import net.k1ra.hoodies_network_kmm.mockwebserver.endpoints.httpbin.Post
import net.k1ra.hoodies_network_kmm.mockwebserver.endpoints.httpbin.Put

object ServerManager {
    private var server: MockWebServerManager? = null

    fun start() {
        val builder = MockWebServerManager.Builder().apply {
            port = 6969
        }

        //HttpBin replica
        builder.addContext("/post", Post())
        builder.addContext("/get", Get())
        builder.addContext("/options", Options())
        builder.addContext("/put", Put())
        builder.addContext("/delete", Delete())
        builder.addContext("/patch", Patch())
        builder.addContext("/image", Image())

        //Postman echo replica
        builder.addContext("/echo/{length}", EchoDelay())

        //Cookie testing setup
        builder.addContext("/cookie_factory", CookieFactory())
        builder.addContext("/cookie_inspector", CookieInspector())

        //Interceptor testing setup
        builder.addContext("/wants_key", WantsKeyHeader())

        server = builder.start()
    }

    fun stop() {
        server?.stop()
    }

    const val testBaseUrl = "http://localhost:6969"
}