package no.nav.tpts.mottak.clients

import kotlinx.serialization.Serializable

@Serializable
data class OAuth2AccessTokenResponse(
    val token_type: String,
    var access_token: String,
    val ext_expires_in: Int,
    val expires_in: Int
)