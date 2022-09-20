package no.nav.tiltakspenger.mottak.clients

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.mottak.clients.HttpClient.client

private val wellknownUrl = System.getenv("AZURE_APP_WELL_KNOWN_URL")
private val clientSecret = System.getenv("AZURE_APP_CLIENT_SECRET")
private val clientId = System.getenv("AZURE_APP_CLIENT_ID")

// Change this to whats needed
private val scope = System.getenv("SCOPE") ?: "api://dev-fss.teamdokumenthandtering.saf/.default"

object AzureOauthClient {
    private val wellknown: WellKnown by lazy { runBlocking { client.get(wellknownUrl).body() } }

    private val tokenCache = TokenCache()

    suspend fun getToken(): String {
        val currentToken = tokenCache.token
        if (currentToken != null && !tokenCache.isExpired()) return currentToken

        return client.submitForm(
            url = wellknown.tokenEndpoint,
            formParameters = Parameters.build {
                append("grant_type", "client_credentials")
                append("client_id", clientId)
                append("client_secret", clientSecret)
                append("scope", scope)
            }
        ).body<OAuth2AccessTokenResponse>().let {
            tokenCache.update(
                it.accessToken,
                it.expiresIn.toLong()
            )
            return@let it.accessToken
        }
    }
}

@JvmInline
@Serializable
value class WellKnown(@SerialName("token_endpoint") val tokenEndpoint: String)
