package no.nav.tiltakspenger.mottak

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.UrlJwkProvider
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.routing.routing
import mu.KotlinLogging
import no.nav.tiltakspenger.mottak.applications.applicationRoutes
import no.nav.tiltakspenger.mottak.db.flywayMigrate
import no.nav.tiltakspenger.mottak.health.HealthCheck
import no.nav.tiltakspenger.mottak.health.healthRoutes
import no.nav.tiltakspenger.mottak.joark.JoarkConsumer
import no.nav.tiltakspenger.mottak.joark.createKafkaConsumer
import no.nav.tiltakspenger.mottak.soknad.soknadRoutes
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
            allowHost("localhost:8081")
            allowHost("127.0.0.1:8081")
            allowHost("localhost:3000")
            allowHost("127.0.0.1:3000")
            allowHost("tpts-tiltakspenger-flate.dev.intern.nav.no")
        }
        appRoutes(listOf(joarkConsumer))
    }.start(wait = true)

    Runtime.getRuntime().addShutdownHook(
        Thread {
            LOG.info { "stopping server" }
            server.stop(gracePeriodMillis = 3000, timeoutMillis = 3000)
        }
    )
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
