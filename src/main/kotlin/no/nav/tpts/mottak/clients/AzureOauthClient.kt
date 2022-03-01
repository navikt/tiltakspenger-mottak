package no.nav.tpts.mottak.clients

import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.http.Parameters
import io.ktor.http.ParametersBuilder
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nav.security.token.support.client.core.OAuth2GrantType
import no.nav.security.token.support.client.core.OAuth2ParameterNames
import no.nav.tpts.mottak.clients.HttpClient.client

private val wellknownUrl = System.getenv("AZURE_APP_WELL_KNOWN_URL")
private val clientSecret = System.getenv("AZURE_APP_CLIENT_SECRET")
private val clientId = System.getenv("AZURE_APP_CLIENT_ID")


object AzureOauthClient {
    private val wellknown: WellKnown by lazy { runBlocking { client.get(wellknownUrl) } }

    private val tokenCache = TokenCache()

    suspend fun getToken(scope: Scope): String {
        val currentToken = tokenCache.token
        if (currentToken != null && !tokenCache.isExpired()) return currentToken

        return client.submitForm<OAuth2AccessTokenResponse>(
            url = wellknown.tokenEndpoint,
            formParameters = Parameters.build {
                appendToken(scope)
            }
        ).let {
            tokenCache.update(
                it.accessToken,
                it.expiresIn.toLong()
            )
            return@let it.accessToken
        }
    }

    suspend fun onBehalfOfExchange(accessToken: String, scope: Scope): OAuth2AccessTokenResponse {
        return client.submitForm(
            url = wellknown.tokenEndpoint,
            formParameters = Parameters.build {
                appendTokenExchangeParams(accessToken, scope)
            }
        )
    }
}

fun ParametersBuilder.appendToken(scope: Scope) {
    append("grant_type", "client_credentials")
    append(OAuth2ParameterNames.CLIENT_ID, clientId)
    append(OAuth2ParameterNames.CLIENT_SECRET, clientSecret)
    append(OAuth2ParameterNames.SCOPE, scope.value)
}

fun ParametersBuilder.appendTokenExchangeParams(accessToken: String, scope: Scope) {
    append("grant_type", OAuth2GrantType.JWT_BEARER.value)
    append(OAuth2ParameterNames.CLIENT_ID, clientId)
    append(OAuth2ParameterNames.CLIENT_SECRET, clientSecret)
    append(OAuth2ParameterNames.SCOPE, scope.value)
    append(OAuth2ParameterNames.ASSERTION, accessToken)
    append(OAuth2ParameterNames.REQUESTED_TOKEN_USE, "on_behalf_of")
}

@Serializable
data class WellKnown(
    @SerialName("token_endpoint")
    val tokenEndpoint: String
)
