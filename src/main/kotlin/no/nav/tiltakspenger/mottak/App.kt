package no.nav.tiltakspenger.mottak

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.prometheus.client.hotspot.DefaultExports
import mu.KotlinLogging
import no.nav.tiltakspenger.mottak.Configuration.KafkaConfig
import no.nav.tiltakspenger.mottak.auth.AzureTokenProvider
import no.nav.tiltakspenger.mottak.health.healthRoutes
import no.nav.tiltakspenger.mottak.joark.JoarkReplicator
import no.nav.tiltakspenger.mottak.joark.createKafkaConsumer
import no.nav.tiltakspenger.mottak.joark.createKafkaProducer
import no.nav.tiltakspenger.mottak.saf.SafClient
import no.nav.tiltakspenger.mottak.saf.SafService

fun main() {
    System.setProperty("logback.configurationFile", "egenLogback.xml")
    val log = KotlinLogging.logger {}
    val securelog = KotlinLogging.logger("tjenestekall")

    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        log.error { "Uncaught exception logget i securelog" }
        securelog.error(e) { e.message }
    }
    DefaultExports.initialize()
    log.info { "Starter server" }
    val kafkaConfig = KafkaConfig()
    val tokenProvider = AzureTokenProvider(Configuration.OauthConfig())
    val safService = SafService(safClient = SafClient(Configuration.SafConfig(), getToken = tokenProvider::getToken))
    val joarkReplicator = JoarkReplicator(
        consumer = createKafkaConsumer(config = kafkaConfig),
        producer = createKafkaProducer(config = kafkaConfig),
        safService = safService,
        tptsRapidName = Configuration.tptsRapidName()
    ).also { it.start() }

    val server = embeddedServer(Netty, Configuration.applicationPort()) {
        routing {
            healthRoutes(listOf(joarkReplicator))
        }
    }.start(wait = true)

    Runtime.getRuntime().addShutdownHook(Thread {
        log.info { "Stopper server" }
        server.stop(gracePeriodMillis = 3000, timeoutMillis = 3000)
    })
}
