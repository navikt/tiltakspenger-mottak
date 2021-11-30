package no.nav.tpts.mottak.clients

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.security.token.support.client.core.OAuth2GrantType
import java.time.Instant
import java.util.*

private val wellKnown = System.getenv("TOKEN_X_WELL_KNOWN_URL")
private val clientId = System.getenv("TOKEN_X_CLIENT_ID")
private val privateJwk = System.getenv("TOKEN_X_PRIVATE_JWK")
private const val tokenXAudience = "https://tokendings.dev-gcp.nais.io/token"

object TokenXClient {
    private var wellknown: WellKnown = runBlocking { httpClient.get(wellKnown) }

    suspend fun exchangeToken(accessToken: String, targetAudience: String): OAuth2AccessTokenResponse {
        return httpClient.submitForm(
            url = wellknown.token_endpoint,
            formParameters = Parameters.build {
                append("grant_type", OAuth2GrantType.TOKEN_EXCHANGE .value)
                append("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
                append("client_assertion", createClientAssertion(clientId, tokenXAudience))
                append("subject_token_type", "urn:ietf:params:oauth:token-type:jwt")
                append("subject_token", accessToken)
                append("audience", targetAudience)
            }) {}
    }
}

private val rsaKey = RSAKey.parse(privateJwk)

fun createClientAssertion(clientId: String, tokenXAudience: String): String {
    val now = Date.from(Instant.now())
    return JWTClaimsSet.Builder()
        .subject(clientId)
        .issuer(clientId)
        .audience(tokenXAudience)
        .issueTime(now)
        .notBeforeTime(now)
        .expirationTime(Date.from(Instant.now().plusSeconds(60)))
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