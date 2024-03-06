package net.k1ra.hoodies_network_kmm.cache.configuration

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

class CacheEnabled(
    val staleDataThreshold: Duration = 1.hours,
    val encryptionEnabled: Boolean = false,
) : CacheConfiguration