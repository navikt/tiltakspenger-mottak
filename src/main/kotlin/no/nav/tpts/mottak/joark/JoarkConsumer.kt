@file:Suppress("TooGenericExceptionCaught", "MagicNumber")

package no.nav.tpts.mottak.joark

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mu.KotlinLogging
import no.nav.tpts.mottak.health.HealthCheck
import no.nav.tpts.mottak.health.HealthStatus
import no.nav.tpts.mottak.soknad.handleSoknad
import no.nav.tpts.mottak.topicName
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.errors.WakeupException
import org.apache.kafka.common.security.auth.SecurityProtocol
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.Properties

private val LOG = KotlinLogging.logger {}

const val MAX_POLL_RECORDS = 50
const val MAX_POLL_INTERVAL_MS = 5000
private val POLL_TIMEOUT = Duration.ofSeconds(4)

fun createKafkaConsumer(): KafkaConsumer<String, GenericRecord> {
    return KafkaConsumer<String, GenericRecord>(
        Properties().also {
            it[ConsumerConfig.GROUP_ID_CONFIG] = "tpts-tiltakspenger-aiven-mottak-v6"
            it[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
            it[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
            it[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaAvroDeserializer::class.java
            it[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "false"
            it[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = MAX_POLL_RECORDS
            it[ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG] = MAX_POLL_INTERVAL_MS
            it[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = SecurityProtocol.SSL.name
            it[SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG] = ""
            it[SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG] = "jks"
            it[SslConfigs.SSL_KEYSTORE_TYPE_CONFIG] = "PKCS12"
            it[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = "/var/run/secrets/nais.io/kafka/client.truststore.jks"
            it[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = System.getenv("KAFKA_CREDSTORE_PASSWORD")
            it[SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = "/var/run/secrets/nais.io/kafka/client.keystore.p12"
            it[SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG] = System.getenv("KAFKA_CREDSTORE_PASSWORD")
            it[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = System.getenv("KAFKA_BROKERS")
            it[AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG] = System.getenv("KAFKA_SCHEMA_REGISTRY")
            it[SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE] = "USER_INFO"
            it[SchemaRegistryClientConfig.USER_INFO_CONFIG] =
                System.getenv("KAFKA_SCHEMA_REGISTRY_USER") + ":" + System.getenv("KAFKA_SCHEMA_REGISTRY_PASSWORD")
        }
    ).also { it.subscribe(listOf(topicName())) }
}

internal class JoarkConsumer(
    private val consumer: Consumer<String, GenericRecord>,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : HealthCheck {
    private lateinit var job: Job

    init {
        Runtime.getRuntime().addShutdownHook(Thread(::shutdownHook))
    }

    override fun status(): HealthStatus = if (job.isActive) HealthStatus.TILFREDS else HealthStatus.ULYKKELIG

    fun start() {
        LOG.info { "starting JoarkConsumer" }
        job = scope.launch {
            run()
        }
    }

    fun stop() {
        LOG.info { "stopping JoarkConsumer" }
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
        LOG.debug { "records received: ${records.count()}" }
        if (records.isEmpty) return // poll returns an empty collection in case of rebalancing
        var currentPartitionOffsets: MutableMap<TopicPartition, Long> = mutableMapOf()
        try {
            currentPartitionOffsets = records
                .groupBy { TopicPartition(it.topic(), it.partition()) }
                .mapValues { partition -> partition.value.minOf { it.offset() } }
                .toMutableMap()
            records.onEach { record ->
                if (isCorrectTemaAndStatus(record)) {
                    LOG.info { "Mottok joark-melding: $record" }
                    scope.launch {
                        LOG.debug { "retreiving soknad" }
                        handleSoknad(record.key())
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

    private fun isCorrectTemaAndStatus(record: ConsumerRecord<String, GenericRecord>) =
        (record.value().get("temaNytt")?.toString() ?: "") == "IND" &&
            (record.value().get("journalpostStatus")?.toString() ?: "") == "MOTTATT"

    private fun closeResources() {
        LOG.info { "close resources" }
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
        LOG.info("received shutdown signal, stopping app")
        stop()
    }
}
