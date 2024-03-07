package net.k1ra.hoodies_network_kmm.core

import korlibs.encoding.Hex

object FormUrlEncoder {
    fun decode(str: String): Map<String, String> {
        val out = mutableMapOf<String, String>()
        str.split('&').forEach { chunk ->
            val parts = chunk.split('=', limit = 2)
            val key = decodeComponent(parts[0])
            out[key] = decodeComponent(parts.getOrElse(1) { key })
        }
        return out
    }

    fun encode(queryParams: Map<String, String>?): String {
        val parts = arrayListOf<String>()
        queryParams?.forEach { (key, value) ->
            parts += encodeComponent(key) + "=" + encodeComponent(value)
        }
        return parts.joinToString("&")
    }

    private fun encodeComponent(s: String): String {
        val sb = StringBuilder(s.length)
        val data = s.encodeToByteArray()
        for (element in data) {
            when (val cc = element.toInt().toChar()) {
                ' ' -> sb.append("+")
                in 'a'..'z', in 'A'..'Z', in '0'..'9', '-', '_', '.', '*' -> sb.append(cc)
                else -> {
                    sb.append('%')
                    for (n in 1 downTo 0) sb.append(
                        Hex.encodeCharUpper(
                            element.toInt().ushr(n * 4) and 0xF
                        )
                    )
                }
            }
        }
        return sb.toString()
    }

    private fun decodeComponent(s: String): String {
        val bos = arrayListOf<Byte>()
        val len = s.length
        var n = 0
        while (n < len) {
            when (val c = s[n]) {
                '%' -> {
                    bos.add(s.substring(n + 1, 2).toInt(16).toByte())
                    n += 2
                }
                '+' -> bos.add(' '.code.toByte())
                else -> bos.add(c.code.toByte())
            }
            n++
        }
        return bos.toByteArray().decodeToString()
    }

}