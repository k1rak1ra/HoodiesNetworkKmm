package testObjects

import kotlinx.serialization.Serializable

@Serializable
class CookieFactoryRequest(
    val name: String,
    val value: String
)