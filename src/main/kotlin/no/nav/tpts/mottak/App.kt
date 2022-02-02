package no.nav.tpts.mottak

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.UrlJwkProvider
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
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
import no.nav.tpts.mottak.joark.JoarkConsumer
import no.nav.tpts.mottak.joark.createKafkaConsumer
import no.nav.tpts.mottak.soknad.soknadRoutes
import java.net.URI

val LOG = KotlinLogging.logger {}
const val PORT = 8080
const val LEEWAY = 3L

fun main() {
    LOG.info { "starting server" }

    flywayMigrate()
    JoarkConsumer(createKafkaConsumer("teamdokumenthandtering.aapen-dok-journalfoering-q1")).also { it.start() }

    val server = embeddedServer(Netty, PORT) {
        acceptJson()
        installAuth()
        install(CORS) {
            host("localhost:8081")
            host("127.0.0.1:8081")
            host("localhost:3000")
            host("127.0.0.1:3000")
            host("tpts-tiltakspenger-flate.dev.intern.nav.no")
        }
        appRoutes()
    }.start()

    Runtime.getRuntime().addShutdownHook(
        Thread {
            LOG.info { "stopping server" }
            server.stop(gracePeriodMillis = 3000, timeoutMillis = 1000)
        }
    )
}

object AuthConfig {
    val issuer = System.getenv("AZURE_ISSUER")
    val jwksUri = System.getenv("AZURE_JWKS_URI")
}

fun Application.installAuth(jwkProvider: JwkProvider = UrlJwkProvider(URI(AuthConfig.jwksUri).toURL())) {
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(jwkProvider, AuthConfig.issuer) {
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
}

fun Application.appRoutes() {
    routing {
        healthRoutes()
        applicationRoutes()
        authenticate("auth-jwt") {
            soknadRoutes()
        }
    }
}

fun Application.acceptJson() {
    install(DefaultHeaders)
    install(ContentNegotiation) {
        json()
    }
}
