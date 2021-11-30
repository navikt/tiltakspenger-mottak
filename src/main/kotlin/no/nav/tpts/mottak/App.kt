package no.nav.tpts.mottak

import com.auth0.jwk.UrlJwkProvider
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import mu.KotlinLogging
import no.nav.tpts.mottak.applications.applicationRoutes
import java.net.URI

val LOG = KotlinLogging.logger {}

fun main() {
    LOG.info { "starting server" }

    val issuer = System.getenv("AZURE_ISSUER")
    val jwksUri = System.getenv("AZURE_JWKS_URI")

    val jwkProvider = UrlJwkProvider(URI(jwksUri).toURL())

    val server = embeddedServer(Netty, 8080) {
        install(DefaultHeaders)
        install(ContentNegotiation) {
            json()
        }
        install(Authentication) {
            jwt("auth-jwt") {
                verifier(jwkProvider, issuer) {
                    acceptLeeway(3)
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
            host("127.0.0.1:3000")
            host("tpts-tiltakspenger-flate.dev.intern.nav.no")
        }
        routing {
            healthRoutes()
            applicationRoutes()
        }
    }.start()

    Runtime.getRuntime().addShutdownHook(
        Thread {
            LOG.info { "stopping server" }
            server.stop(gracePeriodMillis = 3000, timeoutMillis = 1000)
        }
    )
}

fun Route.healthRoutes() {
    route("/metrics") {
        get {
            call.respondTextWriter {
                TextFormat.writeFormat(
                    TextFormat.CONTENT_TYPE_004,
                    this,
                    CollectorRegistry.defaultRegistry.metricFamilySamples()
                )
            }
        }
    }.also { LOG.info { "setting up endpoint /metrics" } }
    route("/isAlive") {
        get {
            call.respondText(text = "ALIVE", contentType = ContentType.Text.Plain)
        }
    }.also { LOG.info { "setting up endpoint /isAlive" } }
    route("/isReady") {
        get {
            call.respondText(text = "READY", contentType = ContentType.Text.Plain)
        }
    }.also { LOG.info { "setting up endpoint /isReady" } }
}

