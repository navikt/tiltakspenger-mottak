package no.nav.tpts.mottak

import com.auth0.jwk.UrlJwkProvider
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import mu.KotlinLogging
import no.nav.tpts.mottak.applications.applicationRoutes
import no.nav.tpts.mottak.db.flywayMigrate
import no.nav.tpts.mottak.health.healthRoutes
import no.nav.tpts.mottak.joark.createJoarkConsumer
import no.nav.tpts.mottak.soknad.soknadRoutes
import java.net.URI

val LOG = KotlinLogging.logger {}
const val PORT = 8080
const val LEEWAY = 3L

fun main() {
    LOG.info { "starting server" }

    flywayMigrate()
    createJoarkConsumer("teamdokumenthandtering.aapen-dok-FOO-journalfoering-q1")

    val issuer = System.getenv("AZURE_ISSUER")
    val jwksUri = System.getenv("AZURE_JWKS_URI")

    val jwkProvider = UrlJwkProvider(URI(jwksUri).toURL())

    val server = embeddedServer(Netty, PORT) {
        acceptJson()
        install(Authentication) {
            jwt("auth-jwt") {
                verifier(jwkProvider, issuer) {
                    acceptLeeway(LEEWAY)
                }
                validate { credential ->
                    if (credential.payload.getClaim("aud").asString() != "http://tpts-mottak.nav.no") {
                        JWTPrincipal(credential.payload)
                    } else {
                        null
                    }
                }
            }
        }
        install(CORS) {
            host("localhost:8081")
            host("127.0.0.1:8081")
            host("localhost:3000")
            host("127.0.0.1:3000")
            host("tpts-tiltakspenger-flate.dev.intern.nav.no")
        }
        routing {
            healthRoutes()
            applicationRoutes()
            soknadRoutes()
        }
    }.start()

    Runtime.getRuntime().addShutdownHook(
        Thread {
            LOG.info { "stopping server" }
            server.stop(gracePeriodMillis = 3000, timeoutMillis = 1000)
        }
    )
}

fun Application.acceptJson() {
    install(DefaultHeaders)
    install(ContentNegotiation) {
        json()
    }
}
