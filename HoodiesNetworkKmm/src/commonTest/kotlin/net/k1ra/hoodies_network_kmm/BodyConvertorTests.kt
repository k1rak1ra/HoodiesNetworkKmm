package net.k1ra.hoodies_network_kmm

import com.benasher44.uuid.uuid4
import net.k1ra.hoodies_network_kmm.testObjects.CookieFactoryRequest
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.Test

class BodyConvertorTests {
    private val dummyClient = HoodiesNetworkClient.Builder().build()

    @Test
    fun requestNull() {
        val out = dummyClient.convertRequestBody<String>(null)
        assertEquals("Output is not empty", 0, out.size)
    }

    @Test
    fun requestUnit() {
        val out = dummyClient.convertRequestBody(Unit)
        assertEquals("Output is not empty", 0, out.size)
    }

    @Test
    fun requestByteArray() {
        val input = uuid4().toString().encodeToByteArray()
        val out = dummyClient.convertRequestBody(input)
        assertEquals("Output does not match", input.decodeToString(), out.decodeToString())
    }

    @Test
    fun requestString() {
        val input = uuid4().toString()
        val out = dummyClient.convertRequestBody(input)
        assertEquals("Output does not match", "\"$input\"", out.decodeToString())
    }

    @Test
    fun serializableClassRequestResponse() {
        val input = CookieFactoryRequest(uuid4().toString(), uuid4().toString())
        val out = dummyClient.convertRequestBody(input)
        val outObj = dummyClient.convertResponseBody<CookieFactoryRequest>(out)
        assertEquals("Key does not match", input.name, outObj.name)
        assertEquals("Value does not match", input.value, outObj.value)
    }

    @Test
    fun responseUnit() {
        val input = uuid4().toString().encodeToByteArray()
        val out = dummyClient.convertResponseBody<Unit>(input)
        assertEquals("Output is not Unit", out, Unit)
    }

    @Test
    fun responseByteArray() {
        val input = uuid4().toString().encodeToByteArray()
        val out = dummyClient.convertResponseBody<ByteArray>(input)
        assertEquals("Output does not match", out.decodeToString(), input.decodeToString())
    }

    @Test
    fun responseString() {
        val input = uuid4().toString().encodeToByteArray()
        val out = dummyClient.convertResponseBody<String>(input)
        assertEquals("Output does not match", out, input.decodeToString())
    }
}