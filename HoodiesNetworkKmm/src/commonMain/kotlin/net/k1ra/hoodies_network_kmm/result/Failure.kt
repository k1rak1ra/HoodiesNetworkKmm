package net.k1ra.hoodies_network_kmm.result

data class Failure(val reason: HttpClientError) : Result<Nothing>