package net.k1ra.hoodies_network_kmm.config

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

data class HttpClientConfig (
    val connectTimeout: Duration = 30.seconds,
    val readTimeout: Duration = 30.seconds
) {
    companion object {
        //Forces use of in-memory DB for sqlite and encryption key storage in Android (Because we can't use a Context)
        var testMode = false
    }
}