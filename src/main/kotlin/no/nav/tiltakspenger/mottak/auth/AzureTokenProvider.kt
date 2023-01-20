package no.nav.tiltakspenger.mottak.auth

import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.http.Parameters
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.mottak.Configuration
import no.nav.tiltakspenger.mottak.HttpClient.client

class AzureTokenProvider(private val config: Configuration.OauthConfig) {
    private val wellknown: WellKnown by lazy { runBlocking { client.get(config.wellknownUrl).body() } }
    private val tokenCache = TokenCache()

    suspend fun getToken(): String {
        val currentToken = tokenCache.token
        if (currentToken != null && !tokenCache.isExpired()) return currentToken
        val response: OAuth2AccessTokenResponse = client.submitForm(
            url = wellknown.tokenEndpoint,
            formParameters = Parameters.build {
                append("grant_type", "client_credentials")
                append("client_id", config.clientId)
                append("client_secret", config.clientSecret)
                append("scope", config.scope)
            }
        ).body()
        tokenCache.update(response.accessToken, response.expiresIn.toLong())
        return response.accessToken
    }

    @Serializable
    data class WellKnown(
        @SerialName("token_endpoint")
        val tokenEndpoint: String
    )
}
