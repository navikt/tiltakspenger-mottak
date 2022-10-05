package no.nav.tiltakspenger.mottak.clients

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.mottak.Configuration
import no.nav.tiltakspenger.mottak.clients.HttpClient.client

class AzureOauthClient(private val config: Configuration.OauthConfig) {
    private val wellknown: WellKnown by lazy { runBlocking { client.get(config.wellknownUrl).body() } }
    private val tokenCache = TokenCache()

    suspend fun getToken(): String {
        val currentToken = tokenCache.token
        if (currentToken != null && !tokenCache.isExpired()) return currentToken

        return client.submitForm(
            url = wellknown.tokenEndpoint,
            formParameters = Parameters.build {
                append("grant_type", "client_credentials")
                append("client_id", config.clientId)
                append("client_secret", config.clientSecret)
                append("scope", config.scope)
            }
        ).body<OAuth2AccessTokenResponse>().let {
            tokenCache.update(
                it.accessToken,
                it.expiresIn.toLong()
            )
            return@let it.accessToken
        }
    }

    @Serializable
    data class WellKnown(
        @SerialName("token_endpoint")
        val tokenEndpoint: String
    )
}
