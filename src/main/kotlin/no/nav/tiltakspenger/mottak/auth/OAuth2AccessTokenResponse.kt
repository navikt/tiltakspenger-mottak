package no.nav.tiltakspenger.mottak.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OAuth2AccessTokenResponse(
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("access_token")
    var accessToken: String,
    @SerialName("ext_expires_in")
    val extExpiresIn: Int,
    @SerialName("expires_in")
    val expiresIn: Int,
)
