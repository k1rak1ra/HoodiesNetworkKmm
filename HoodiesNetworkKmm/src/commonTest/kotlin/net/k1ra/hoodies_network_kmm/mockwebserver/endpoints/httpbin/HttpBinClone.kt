package net.k1ra.hoodies_network_kmm.mockwebserver.endpoints.httpbin

import net.k1ra.hoodies_network_kmm.mockwebserver.HttpCall
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive


class HttpBinClone {
    fun handleRequest(call: HttpCall) : String {
        val response = mutableMapOf<String, JsonElement>()
        val headers = mutableMapOf<String, JsonElement>()
        val files = mutableMapOf<String, JsonElement>()

        if (call.getHeaders()["Content-type"]?.firstOrNull()?.toString()?.contains("multipart/form-data;") == true) {
            val boundary = call.getHeaders()["Content-type"]?.firstOrNull()?.toString()?.split("boundary=")?.last()!!

            val bodyParts = call.getBodyString().split("--$boundary")

            for (part in bodyParts){
                val lines = part.split("\n")
                if (lines.size > 3) {
                    val fileName = lines[1].split(" name=").last().split(";").first()
                    val fileContents = StringBuilder()
                    lines.subList(3, lines.size).forEach { if (it.isNotBlank()) { fileContents.append(it.replace("\r","")) } }

                    files[fileName] = JsonPrimitive(fileContents.toString())
                }
            }
        }

        response["files"] = JsonObject(files)

        for (item in call.getHeaders()) {
            headers[item.key] = JsonPrimitive(item.value)
        }

        response["headers"] = JsonObject(headers)
        response["url"] =  JsonPrimitive("http://localhost:6969${call.path}")

        if (call.getHeaders()["Content-Type"]?.firstOrNull().toString() == "application/x-www-form-urlencoded") {
            response["data"] = JsonPrimitive(call.path.split("?")[1])
        } else {
            response["data"] = JsonPrimitive(call.getBodyString())
        }

        try {
            val args = mutableMapOf<String, JsonElement>()
            for (item in call.getFormUrlEncodedParameters()) {
                args[item.key] = JsonPrimitive(item.value)
            }
            response["args"] = JsonObject(args)
        } catch (e: Exception) {
            //Unsupported
        }

        return JsonObject(response).toString()
    }
}