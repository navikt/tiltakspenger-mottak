package no.nav.tiltakspenger.mottak.health

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.ServiceUnavailable
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.response.respondTextWriter
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import mu.KotlinLogging

val LOG = KotlinLogging.logger { }

fun Route.healthRoutes(healthChecks: List<HealthCheck>) {
    route("/metrics") {
        get {
            call.respondTextWriter(contentType = ContentType.parse(TextFormat.CONTENT_TYPE_004), status = OK) {
                TextFormat.write004(this, CollectorRegistry.defaultRegistry.metricFamilySamples())
            }
        }
    }.also { LOG.info { "satt opp endepunkt /metrics" } }
    route("/isalive") {
        get {
            val failedHealthChecks = healthChecks.filter { it.status() == HealthStatus.ULYKKELIG }
            if (failedHealthChecks.isNotEmpty()) {
                LOG.warn { "Failed health checks: $failedHealthChecks" }
                call.respondText(text = "DEAD", contentType = ContentType.Text.Plain, status = ServiceUnavailable)
            } else {
                call.respondText(text = "ALIVE", contentType = ContentType.Text.Plain, status = OK)
            }
        }
    }.also { LOG.info { "satt opp endepunkt /isalive" } }
    route("/isready") {
        get {
            call.respondText(text = "READY", contentType = ContentType.Text.Plain)
        }
    }.also { LOG.info { "satt opp endepunkt /isready" } }
}
