package net.k1ra.hoodies_network_kmm.mockwebserver

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.net.ServerSocket

internal actual class ServerSocketFactory {
    private var run = true
    private var socket: ServerSocket? = null

    actual fun stopServerSocket() {
        run = false

        socket?.close()
        socket = null
    }

    actual fun createServerSocket(port: Int,  callConsumer: suspend (HttpCall) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            socket = ServerSocket(port)

            while (run) {
                try {
                    val connection = socket?.accept() ?: break

                    try {
                        val requestLineComponents = getLine(connection.getInputStream()).split(" ")
                        val method = requestLineComponents[0]
                        val path = requestLineComponents[1]
                        val headers = mutableMapOf<String, String>()

                        var doneParsingHeaders = false
                        while (!doneParsingHeaders) {
                            val headerLine = getLine(connection.getInputStream())
                            if (headerLine.isNotEmpty()) {
                                val indexOfFirstSpace = headerLine.indexOfFirst { it == ' ' }
                                if (headerLine.length > indexOfFirstSpace + 2) {
                                    headers[headerLine.substring(0, indexOfFirstSpace)
                                        .replace(":", "")] = headerLine.substring(
                                        indexOfFirstSpace + 1,
                                        headerLine.length
                                    )
                                }
                            } else {
                                doneParsingHeaders = true
                            }
                        }

                        var requestBody = ByteArray(0)
                        if (headers["Content-Length"] != null) {
                            val contentLength = headers["Content-Length"]?.toInt() ?: 0
                            val buffer = ByteArray(1)

                            for (i in 0 until contentLength) {
                                connection.getInputStream().read(buffer)
                                requestBody += buffer
                            }
                        }

                        try {
                            callConsumer.invoke(HttpCall(
                                method,
                                path,
                                headers.toMap(),
                                requestBody
                            ) {
                                connection.getOutputStream().write(it)
                            })
                        } catch (e: Exception) {
                            e.printStackTrace()

                            val output = "HTTP/1.1 500\r\n" +
                                    "Content-Type: text/html;charset-UTF-8\r\n" +
                                    "Server: HoodiesNetworkKmmMockWebServer\r\n" +
                                    "Content-Length: 41\r\n" +
                                    "\r\n" +
                                    "Exception caught while processing request"

                            connection.getOutputStream().write(output.encodeToByteArray())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        connection.close()
                    }
                } catch (_: Exception) {
                    //Socket forcibly closed
                }
            }
        }
    }

    private fun getLine(inputStream: InputStream): String {
        val buffer = StringBuilder()
        var byteBuffer: Int = inputStream.read()
        if (byteBuffer < 0) return ""
        do {
            if (byteBuffer != '\r'.code) {
                buffer.append(byteBuffer.toChar())
            }
            byteBuffer = inputStream.read()
        } while (byteBuffer != '\n'.code && byteBuffer >= 0)
        return buffer.toString()
    }
}