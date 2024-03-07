package net.k1ra.hoodies_network_kmm.cookies

class HttpCookie {
    val name: String
    val value: String

    constructor(headerLine: String) {
        //Get section between Set-Cookie: and first semicolon
        //This should just be the cookie name and value
        val parts = if (headerLine.startsWith("Set-Cookie"))
            headerLine.substring(12).split(";")[0].split("=")
        else
            headerLine.split(";")[0].split("=")

        name = parts[0]
        value = parts[1]
    }

    constructor(name: String, value: String) {
        this.name = name
        this.value = value
    }
}