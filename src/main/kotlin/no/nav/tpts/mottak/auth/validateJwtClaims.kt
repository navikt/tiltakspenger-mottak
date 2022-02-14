package no.nav.tpts.mottak.auth

import io.ktor.application.ApplicationCall
import io.ktor.auth.Principal
import io.ktor.auth.jwt.JWTCredential
import io.ktor.auth.jwt.JWTPrincipal

val validateJwtClaims: (String, String) -> ApplicationCall.(JWTCredential) -> Principal? =
    { clientId, issuer ->
        { credentials ->
            when (credentials.audience.contains(clientId) && credentials.issuer == issuer) {
                true -> JWTPrincipal(payload = credentials.payload)
                else -> null
            }
        }
    }
