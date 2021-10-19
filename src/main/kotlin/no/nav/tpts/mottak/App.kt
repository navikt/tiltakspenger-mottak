package no.nav.tpts.mottak

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import mu.KotlinLogging

private val LOG = KotlinLogging.logger {}

fun main() {
    LOG.info { "starting server" }
    val server = embeddedServer(Netty, 8080) {
        install(DefaultHeaders)
        routing {
            healthRoutes()
        }
    }.start()

    Runtime.getRuntime().addShutdownHook(
        Thread {
            LOG.info { "stopping server" }
            server.stop(gracePeriodMillis = 3000, timeoutMillis = 5000)
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

