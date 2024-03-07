package net.k1ra.hoodies_network_kmm.mockwebserver

import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.get
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import platform.posix.AF_INET
import platform.posix.AI_PASSIVE
import platform.posix.IPPROTO_TCP
import platform.posix.SOCK_STREAM
import platform.posix.accept
import platform.posix.addrinfo
import platform.posix.bind
import platform.posix.close
import platform.posix.getaddrinfo
import platform.posix.listen
import platform.posix.read
import platform.posix.sockaddr
import platform.posix.socket
import platform.posix.socklen_tVar
import platform.posix.write

@OptIn(ExperimentalForeignApi::class)
internal actual class ServerSocketFactory {
    private var run = true
    private var socketFd: Int? = null

    actual fun stopServerSocket() {
        run = false

        if (socketFd != null)
            close(socketFd!!)

        socketFd = null
    }

    actual fun createServerSocket(port: Int,  callConsumer: suspend (HttpCall) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            socketFd = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP)

            val serverInfo = nativeHeap.alloc<CPointerVar<addrinfo>>()
            val addrHints = nativeHeap.alloc<addrinfo>()
            addrHints.ai_flags = AI_PASSIVE
            addrHints.ai_family = AF_INET
            addrHints.ai_protocol = 0
            addrHints.ai_addrlen = 0u
            addrHints.ai_canonname = null
            addrHints.ai_addr = null
            addrHints.ai_next = null

            getaddrinfo(null, port.toString(), addrHints.ptr, serverInfo.ptr)
            bind(socketFd!!, serverInfo.value!![0].ai_addr, serverInfo.value!![0].ai_addrlen)
            listen(socketFd!!, 5)

            val clientAddr = nativeHeap.alloc<sockaddr>()
            val clientAddrSize = nativeHeap.alloc<socklen_tVar>()
            clientAddrSize.value = 0u

            while (run) {
                val clientFd = accept(socketFd!!, clientAddr.ptr, clientAddrSize.ptr)

                try {
                    val requestLineComponents = getLine(clientFd).split(" ")
                    val method = requestLineComponents[0]
                    val path = requestLineComponents[1]
                    val headers = mutableMapOf<String, String>()

                    var doneParsingHeaders = false
                    while (!doneParsingHeaders) {
                        val headerLine = getLine(clientFd)
                        if (headerLine.isNotEmpty()) {
                            val indexOfFirstSpace = headerLine.indexOfFirst { it == ' ' }
                            if (headerLine.length > indexOfFirstSpace + 2)
                                headers[headerLine.substring(0, indexOfFirstSpace).replace(":", "")] = headerLine.substring(indexOfFirstSpace + 1, headerLine.length)
                        } else {
                            doneParsingHeaders = true
                        }
                    }

                    var requestBody = ByteArray(0)
                    if (headers["Content-Length"] != null) {
                        val contentLength = headers["Content-Length"]?.toInt() ?: 0
                        requestBody = ByteArray(contentLength)

                        requestBody.usePinned {
                            read(clientFd, it.addressOf(0), contentLength.toULong())
                        }
                    }

                    try {
                        callConsumer.invoke(HttpCall(
                            method,
                            path,
                            headers.toMap(),
                            requestBody
                        ){
                            it.usePinned { pinned ->
                                write(
                                    clientFd,
                                    pinned.addressOf(0),
                                    it.size.toULong()
                                )
                            }
                        })
                    } catch(e: Exception) {
                        e.printStackTrace()

                        val output = "HTTP/1.1 200\r\n" +
                                "Content-Type: text/html;charset-UTF-8\r\n" +
                                "Server: HoodiesNetworkKmmMockWebServer\r\n" +
                                "Content-Length: 28\r\n" +
                                "\r\n" +
                                "<html><body>OK</body></html>"

                        output.encodeToByteArray().usePinned {
                            write(
                                clientFd,
                                it.addressOf(0),
                                output.encodeToByteArray().size.toULong()
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    close(clientFd)
                }
            }
        }
    }

    private fun getLine(clientFd: Int): String {
        val buffer = StringBuilder()
        val byteBuffer = ByteArray(1)
        byteBuffer.usePinned {
            read(clientFd, it.addressOf(0), byteBuffer.size.toULong())
        }

        if (byteBuffer[0] < 0) return ""
        do {
            if (byteBuffer[0].toInt() != '\r'.code) {
                buffer.append(byteBuffer[0].toInt().toChar())
            }

            byteBuffer.usePinned {
                read(clientFd, it.addressOf(0), byteBuffer.size.toULong())
            }

        } while (byteBuffer[0].toInt() != '\n'.code && byteBuffer[0].toInt() >= 0)
        return buffer.toString()
    }
}