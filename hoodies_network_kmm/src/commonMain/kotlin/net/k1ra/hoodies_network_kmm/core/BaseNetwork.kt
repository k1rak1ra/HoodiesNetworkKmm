package net.k1ra.hoodies_network_kmm.core

import net.k1ra.hoodies_network_kmm.config.HttpClientConfig
import net.k1ra.hoodies_network_kmm.request.NetworkRequest
import net.k1ra.hoodies_network_kmm.request.NetworkResponse


expect class BaseNetwork() {
    suspend fun execRequest(request: NetworkRequest, config: HttpClientConfig) : NetworkResponse
}