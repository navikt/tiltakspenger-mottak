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
import no.nav.tpts.mottak.health.HealthCheck
import no.nav.tpts.mottak.health.healthRoutes
import no.nav.tpts.mottak.joark.JoarkConsumer
import no.nav.tpts.mottak.joark.createKafkaConsumer
import no.nav.tpts.mottak.soknad.soknadRoutes
import java.net.URI

private val LOG = KotlinLogging.logger {}
const val PORT = 8080
const val LEEWAY = 3L

fun main() {
    LOG.info { "starting server" }
    Thread.setDefaultUncaughtExceptionHandler { _, e -> LOG.error(e) { e.message } }
    flywayMigrate()
    val joarkConsumer = JoarkConsumer(createKafkaConsumer()).also { it.start() }

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
        appRoutes(listOf(joarkConsumer))
    }.start()

    Runtime.getRuntime().addShutdownHook(
        Thread {
            LOG.info { "stopping server" }
            server.stop(gracePeriodMillis = 3000, timeoutMillis = 3000)
        }
    )
    LOG.warn { "Tester logging" }
    try {
        throw IllegalArgumentException("Catch: Test for å se om dette kommer til stderr eller ikke\n og en ny rad her")
    } catch (e: IllegalArgumentException) {
        LOG.error(e) { e.message }
    }
    throw IllegalArgumentException("NoCatch: Test for å se om dette kommer til stderr eller ikke\n og en ny rad her")
}

fun Application.installAuth(jwkProvider: JwkProvider = UrlJwkProvider(URI(AuthConfig.jwksUri).toURL())) {
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(jwkProvider, AuthConfig.issuer) {
                withAudience(AuthConfig.clientId)
                acceptLeeway(LEEWAY)
            }
            validate { jwtCredential -> JWTPrincipal(jwtCredential.payload) }
        }
    }
}

fun Application.appRoutes(healthChecks: List<HealthCheck>) {
    routing {
        healthRoutes(healthChecks)
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
