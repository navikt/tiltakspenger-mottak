package no.nav.tpts.mottak

import com.auth0.jwk.UrlJwkProvider
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.features.CORS
import io.ktor.features.DefaultHeaders
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.routing
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.routing.get
import io.ktor.serialization.json
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import mu.KotlinLogging
import no.nav.tpts.mottak.applications.applicationRoutes
import no.nav.tpts.mottak.database.migrate
import java.net.URI

val LOG = KotlinLogging.logger {}
// TODO: Temporary setup, should consider doing a on behalf of exchange in frontend before
val FRONTEND_CLIENT_ID = "8cfaf79d-b131-4092-8227-32b3fa92ca82"

fun main() {
    LOG.info { "starting server" }

    migrate()

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
                    if (credential.payload.getClaim("aud").asString() == FRONTEND_CLIENT_ID) {
                        JWTPrincipal(credential.payload)
                    } else {
                        null
                    }
                }
            }
        }
        /**/
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
