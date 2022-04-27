package no.nav.tpts.mottak.clients

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.http.Parameters
import kotlinx.coroutines.runBlocking
import no.nav.security.token.support.client.core.OAuth2GrantType
import no.nav.tpts.mottak.clients.HttpClient.client
import java.time.Instant
import java.util.Date
import java.util.UUID

private val wellKnown = System.getenv("TOKEN_X_WELL_KNOWN_URL")
private val clientId = System.getenv("TOKEN_X_CLIENT_ID")
private val privateJwk = System.getenv("TOKEN_X_PRIVATE_JWK")
private const val TOKENX_AUDIENCE = "https://tokendings.dev-gcp.nais.io/token"

object TokenXClient {
    private var wellknown: WellKnown = runBlocking { client.get(wellKnown).body() }

    suspend fun exchangeToken(accessToken: String, targetAudience: String): OAuth2AccessTokenResponse {
        return client.submitForm(
            url = wellknown.tokenEndpoint,
            formParameters = Parameters.build {
                append("grant_type", OAuth2GrantType.TOKEN_EXCHANGE.value)
                append("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
                append("client_assertion", createClientAssertion(clientId, TOKENX_AUDIENCE))
                append("subject_token_type", "urn:ietf:params:oauth:token-type:jwt")
                append("subject_token", accessToken)
                append("audience", targetAudience)
            }
        ).body()
    }
}

private val rsaKey = RSAKey.parse(privateJwk)

const val EXPIRATION_SLACK = 60L

fun createClientAssertion(clientId: String, tokenXAudience: String): String {
    val now = Date.from(Instant.now())
    return JWTClaimsSet.Builder()
        .subject(clientId)
        .issuer(clientId)
        .audience(tokenXAudience)
        .issueTime(now)
        .notBeforeTime(now)
        .expirationTime(Date.from(Instant.now().plusSeconds(EXPIRATION_SLACK)))
        .jwtID(UUID.randomUUID().toString())
        .build()
        .sign(rsaKey)
        .serialize()
}

private fun JWTClaimsSet.sign(rsaKey: RSAKey): SignedJWT =
    SignedJWT(
        JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(rsaKey.keyID)
            .type(JOSEObjectType.JWT).build(),
        this
    ).apply {
        sign(RSASSASigner(rsaKey.toPrivateKey()))
    }
