import net.k1ra.hoodies_network_kmm.mockwebserver.MockWebServerManager
import endpoints.CookieFactory
import endpoints.CookieInspector
import endpoints.EchoDelay
import endpoints.Image
import endpoints.WantsKeyHeader
import endpoints.httpbin.Delete
import endpoints.httpbin.Get
import endpoints.httpbin.Options
import endpoints.httpbin.Patch
import endpoints.httpbin.Post
import endpoints.httpbin.Put
import kotlinx.coroutines.delay

object ServerManager {
    private var server: MockWebServerManager? = null

    suspend fun start() {
        val builder = MockWebServerManager.Builder()

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

        //Sometimes the tests get run in parallel and fail because the port is already in use
        //For those cases, we will wait here until the server can start

        var started = false

        while (!started) {
            try {
                server = builder.start()
                started = true
            } catch (e: Exception) {
                delay(100)
            }
        }
    }

    fun stop() {
        server?.stop()
    }
}