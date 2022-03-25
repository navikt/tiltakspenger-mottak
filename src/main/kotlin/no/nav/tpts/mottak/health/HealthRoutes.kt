package no.nav.tpts.mottak.health

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.ServiceUnavailable
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import org.apache.logging.log4j.kotlin.logger

private val LOG = logger("no.nav.tpts.mottak.health.HealthRoutes")

fun Route.healthRoutes(healthChecks: List<HealthCheck>) {
    route("/metrics") {
        get {
            call.respondTextWriter(contentType = ContentType.parse(TextFormat.CONTENT_TYPE_004), status = OK) {
                TextFormat.write004(this, CollectorRegistry.defaultRegistry.metricFamilySamples())
            }
        }
    }.also { LOG.info { "setting up endpoint /metrics" } }
    route("/isAlive") {
        get {
            val failedHealthChecks = healthChecks.filter { it.status() == HealthStatus.ULYKKELIG }
            if (failedHealthChecks.isNotEmpty()) {
                LOG.warn { "Failed health checks: $failedHealthChecks" }
                call.respondText(text = "DEAD", contentType = ContentType.Text.Plain, status = ServiceUnavailable)
            } else {
                call.respondText(text = "ALIVE", contentType = ContentType.Text.Plain, status = OK)
            }
        }
    }.also { LOG.info { "setting up endpoint /isAlive" } }
    route("/isReady") {
        get {
            call.respondText(text = "READY", contentType = ContentType.Text.Plain)
        }
    }.also { LOG.info { "setting up endpoint /isReady" } }
}
