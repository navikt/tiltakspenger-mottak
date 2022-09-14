package no.nav.tiltakspenger.mottak

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import no.nav.tiltakspenger.mottak.health.healthRoutes
import no.nav.tiltakspenger.mottak.joark.JoarkReplicator
import no.nav.tiltakspenger.mottak.joark.createKafkaConsumer
import no.nav.tiltakspenger.mottak.joark.createKafkaProducer

const val PORT = 8080

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
        routing {
            healthRoutes(listOf(joarkReplicator))
        }
    }.start(wait = true)

    Runtime.getRuntime().addShutdownHook(Thread {
        log.info { "stopping server" }
        server.stop(gracePeriodMillis = 3000, timeoutMillis = 3000)
    })
}
