package no.nav.tpts.mottak.health

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import no.nav.tpts.mottak.LOG

fun Route.healthRoutes() {
    route("/metrics") {
        get {
            call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004), io.ktor.http.HttpStatusCode.OK) {
                TextFormat.write004(this, CollectorRegistry.defaultRegistry.metricFamilySamples())
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
