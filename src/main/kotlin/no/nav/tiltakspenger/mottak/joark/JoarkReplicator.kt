@file:Suppress("TooGenericExceptionCaught", "MagicNumber")

package no.nav.tiltakspenger.mottak.joark

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.prometheus.client.Counter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.tiltakspenger.mottak.Configuration
import no.nav.tiltakspenger.mottak.INDIVIDSTONAD
import no.nav.tiltakspenger.mottak.health.HealthCheck
import no.nav.tiltakspenger.mottak.health.HealthStatus
import no.nav.tiltakspenger.mottak.saf.SafService
import no.nav.tiltakspenger.mottak.søknad.Søknad
import no.nav.tiltakspenger.mottak.unleash
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.errors.WakeupException
import org.apache.kafka.common.security.auth.SecurityProtocol
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import java.time.Duration
import java.util.*

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")
private val POLL_TIMEOUT = Duration.ofSeconds(4)

fun createKafkaConsumer(config: Configuration.KafkaConfig): KafkaConsumer<String, GenericRecord> {
    return KafkaConsumer<String, GenericRecord>(
        Properties().also {
            it[ConsumerConfig.GROUP_ID_CONFIG] = config.consumerGroupId
            it[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = config.resetPolicy
            it[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
            it[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaAvroDeserializer::class.java
            it[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "false"
            it[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = config.maxPollRecords
            it[ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG] = config.maxPollIntervalMs
            it[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = SecurityProtocol.SSL.name
            it[SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG] = ""
            it[SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG] = "jks"
            it[SslConfigs.SSL_KEYSTORE_TYPE_CONFIG] = "PKCS12"
            it[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = "/var/run/secrets/nais.io/kafka/client.truststore.jks"
            it[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = config.credstorePassword
            it[SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = "/var/run/secrets/nais.io/kafka/client.keystore.p12"
            it[SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG] = config.credstorePassword
            it[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = config.brokers
            it[AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG] = config.schemaRegistry
            it[SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE] = "USER_INFO"
            it[SchemaRegistryClientConfig.USER_INFO_CONFIG] =
                "${config.schemaRegistryUser}:${config.schemaRegistryPassword}"
        }
    ).also { it.subscribe(listOf(config.joarkTopic)) }
}

fun createKafkaProducer(config: Configuration.KafkaConfig): KafkaProducer<String, String> {
    return KafkaProducer(
        Properties().also {
            it[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = config.brokers
            it[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = SecurityProtocol.SSL.name
            it[SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG] = ""
            it[SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG] = "jks"
            it[SslConfigs.SSL_KEYSTORE_TYPE_CONFIG] = "PKCS12"
            it[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = config.truststorePath
            it[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = config.credstorePassword
            it[SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = config.keystorePath
            it[SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG] = config.credstorePassword
            it[ProducerConfig.ACKS_CONFIG] = "all"
            it[ProducerConfig.LINGER_MS_CONFIG] = "0"
            it[ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION] = "1"
        },
        StringSerializer(),
        StringSerializer()
    )
}

internal class JoarkReplicator(
    private val consumer: Consumer<String, GenericRecord>,
    private val producer: Producer<String, String>,
    private val safService: SafService,
    private val tptsRapidName: String,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : HealthCheck {
    private lateinit var job: Job

    init {
        Runtime.getRuntime().addShutdownHook(Thread(::shutdownHook))
    }

    companion object {
        val søknadCounter: Counter =
            Counter.build().name("tiltakspenger_soknader_total").help("Antall soknader").register()
        private val poisonJournalposts = setOf("592543909", "594081377")
    }

    override fun status(): HealthStatus = if (job.isActive) HealthStatus.TILFREDS else HealthStatus.ULYKKELIG

    fun start() {
        LOG.info { "Starter JoarkReplicator" }
        job = scope.launch {
            run()
        }
    }

    fun stop() {
        LOG.info { "Stopper JoarkReplicator" }
        consumer.wakeup()
        job.cancel()
    }

    private fun run() {
        try {
            while (job.isActive) {
                onRecords(consumer.poll(POLL_TIMEOUT))
            }
        } catch (e: WakeupException) {
            if (job.isActive) throw e
        } catch (e: Exception) {
            LOG.error(e) { "Noe feil skjedde i konsumeringen" }
            throw e
        } finally {
            closeResources()
        }
    }

    private fun onRecords(records: ConsumerRecords<String, GenericRecord>) {
        LOG.debug { "Records mottatt: ${records.count()}" }
        if (records.isEmpty) return // poll returns an empty collection in case of rebalancing
        var currentPartitionOffsets: MutableMap<TopicPartition, Long> = mutableMapOf()
        try {
            currentPartitionOffsets = records
                .groupBy { TopicPartition(it.topic(), it.partition()) }
                .mapValues { partition -> partition.value.minOf { it.offset() } }
                .toMutableMap()
            records.onEach { record ->
                if (isCorrectTemaAndStatus(record)) {
                    LOG.info { "Mottok joark-melding" }
                    SECURELOG.info { "Mottok joark-melding: $record" }
                    runBlocking {
                        val soknad = safService.hentSøknad(record.key())
                        if (soknad != null) {
                            val json = createJsonMessage(soknad)
                            LOG.debug { "Sender event på $tptsRapidName med key ${record.key()}" }
                            if (unleash.isEnabled("tiltakspenger.soknad.mottak")) {
                                SECURELOG.info("Sender melding $json")
                                søknadCounter.inc()
                                producer.send(ProducerRecord(tptsRapidName, record.key(), json))
                            } else {
                                SECURELOG.info("Sender ikke melding (stoppet av unleash) $json")
                            }
                        }
                    }
                }
                currentPartitionOffsets[TopicPartition(record.topic(), record.partition())] = record.offset() + 1
            }
        } catch (exception: Exception) {
            val msg = currentPartitionOffsets.map { "\tpartition=${it.key}, offset=${it.value}" }
                .joinToString(separator = "\n", prefix = "\n", postfix = "\n")
            LOG.info(exception) {
                "Processing error, reset positions to each next message (after each record that was processed OK): $msg"
            }
            currentPartitionOffsets.forEach { (partition, offset) -> consumer.seek(partition, offset) }
            throw exception
        } finally {
            consumer.commitSync()
        }
    }

    private fun createJsonMessage(søknad: Søknad) =
        JsonMessage.newMessage(eventName = "søknad_mottatt", mapOf("søknad" to søknad)).toJson()

    private fun isCorrectTemaAndStatus(record: ConsumerRecord<String, GenericRecord>) =
        INDIVIDSTONAD == (record.value().get("temaNytt"))
                && "MOTTATT" == (record.value().get("journalpostStatus"))
                && !poisonJournalposts.contains(record.key())

    private fun closeResources() {
        LOG.info { "Lukker ressurser" }
        tryAndLog(producer::close)
        tryAndLog(consumer::unsubscribe)
        tryAndLog(consumer::close)
    }

    private fun tryAndLog(block: () -> Unit) {
        try {
            block()
        } catch (err: Exception) {
            LOG.error { err.message }
        }
    }

    private fun shutdownHook() {
        LOG.info("Mottok shutdown signal, stopper app")
        stop()
    }
}
