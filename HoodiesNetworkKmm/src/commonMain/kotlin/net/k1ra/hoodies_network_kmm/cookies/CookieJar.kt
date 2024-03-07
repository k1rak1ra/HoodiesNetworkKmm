package net.k1ra.hoodies_network_kmm.cookies

/*
Since an HttpClient instance is only supposed to be for a single URL anyways, we don't have to worry about filtering cookies by URL or any other rules
We'll just worry about the key and value
 */
open class CookieJar {
    private val cookies = arrayListOf<HttpCookie>()

    fun add(cookie: HttpCookie) {
        cookies.add(cookie)
    }

    fun get(): MutableList<HttpCookie> {
        return cookies
    }

    fun remove(name: String): Boolean {
        return cookies.removeAll { it.name == name }
    }

    fun removeAll(): Boolean {
        cookies.clear()
        return true
    }
}