package no.nav.tiltakspenger.mottak

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.UrlJwkProvider
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import no.nav.tiltakspenger.mottak.health.healthRoutes
import no.nav.tiltakspenger.mottak.joark.JoarkReplicator
import no.nav.tiltakspenger.mottak.joark.createKafkaConsumer
import no.nav.tiltakspenger.mottak.joark.createKafkaProducer
import java.net.URI

const val PORT = 8080
const val LEEWAY = 3L

fun main() {
    System.setProperty("logback.configurationFile", "egenLogback.xml")
    val log = KotlinLogging.logger {}
    val securelog = KotlinLogging.logger("tjenestekall")

    Thread.currentThread().setUncaughtExceptionHandler { _, e ->
        log.error { "Uncaught exception logget i securelog" }
        securelog.error(e) { e.message }
    }
    log.info { "starting server" }
    val joarkReplicator = JoarkReplicator(createKafkaConsumer(), createKafkaProducer()).also { it.start() }

    val server = embeddedServer(Netty, PORT) {
        installAuth()
        install(CORS) {
            allowHost("localhost:8081")
            allowHost("127.0.0.1:8081")
            allowHost("localhost:3000")
            allowHost("127.0.0.1:3000")
            allowHost("tpts-tiltakspenger-flate.dev.intern.nav.no")
        }
        routing {
            healthRoutes(listOf(joarkReplicator))
        }
    }.start(wait = true)

    Runtime.getRuntime().addShutdownHook(
        Thread {
            log.info { "stopping server" }
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
