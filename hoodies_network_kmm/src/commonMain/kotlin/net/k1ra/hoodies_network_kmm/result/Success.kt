package net.k1ra.hoodies_network_kmm.result

import net.k1ra.hoodies_network_kmm.request.NetworkResponse


data class Success<out T>(val value: T, val rawResponse: NetworkResponse? = null) : Result<T>()