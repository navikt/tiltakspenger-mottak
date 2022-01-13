@file:Suppress("TooGenericExceptionCaught")

package no.nav.tpts.mottak.joark

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.errors.WakeupException
import org.apache.kafka.common.security.auth.SecurityProtocol
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.Properties
import kotlin.coroutines.CoroutineContext

private val LOGGER = KotlinLogging.logger {}

const val MAX_POLL_RECORDS = 50
const val SIXTY = 60
val maxPollIntervalMs = Duration.ofSeconds(SIXTY + MAX_POLL_RECORDS * 2.toLong()).toMillis()
fun createJoarkConsumer(topicName: String): KafkaConsumer<String, GenericRecord> {
    return KafkaConsumer<String, GenericRecord>(
        Properties().also {
            it[ConsumerConfig.GROUP_ID_CONFIG] = "tpts-tiltakspenger-aiven-mottak-v2"
            it[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
            it[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
            it[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaAvroDeserializer::class.java
            it[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "false"
            it[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = MAX_POLL_RECORDS
            it[ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG] = "$maxPollIntervalMs"

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
    ).also { it.subscribe(listOf(topicName)) }
}

internal class JoarkConsumer(private val consumer: Consumer<String, GenericRecord>) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + Job()
    private val job: Job = Job()

    init {
        Runtime.getRuntime().addShutdownHook(Thread(::shutdownHook))
    }

    fun start() {
        LOGGER.info("starting JournalfoeringReplicator")
        launch {
            run()
        }
    }

    private fun stop() {
        LOGGER.info("stopping JournalfoeringReplicator")
        consumer.wakeup()
        job.cancel()
    }

    private fun run() {
        try {
            while (job.isActive) {
                onRecords(consumer.poll(Duration.ofSeconds(1)))
            }
        } catch (e: WakeupException) {
            if (job.isActive) throw e
        } catch (e: Exception) {
            LOGGER.error(e) { "Noe feil skjedde i konsumeringen" }
            throw e
        } finally {
            closeResources()
        }
    }

    private fun onRecords(records: ConsumerRecords<String, GenericRecord>) {
        LOGGER.debug { "onrecords: ${records.count()}" }
        if (records.isEmpty) return // poll returns an empty collection in case of rebalancing
        val currentPositions = records
            .groupBy { TopicPartition(it.topic(), it.partition()) }
            .mapValues { partition -> partition.value.minOf { it.offset() } }
            .toMutableMap()
        try {
            records.onEach { record ->
                val tema = record.value().get("temaNytt")?.toString() ?: ""
                if (tema == "IND" || tema == "TIL") {
                    LOGGER.info { "Mottok tema '$tema'. $record" }
                }
                currentPositions[TopicPartition(record.topic(), record.partition())] = record.offset() + 1
            }
        } catch (err: Exception) {
            LOGGER.info(
                "due to an error during processing, positions are reset to " +
                    "each next message (after each record that was processed OK):" +
                    currentPositions.map { "\tpartition=${it.key}, offset=${it.value}" }
                        .joinToString(separator = "\n", prefix = "\n", postfix = "\n"),
                err
            )
            currentPositions.forEach { (partition, offset) -> consumer.seek(partition, offset) }
            throw err
        } finally {
            consumer.commitSync()
        }
    }

    private fun closeResources() {
        tryAndLog(consumer::unsubscribe)
        tryAndLog(consumer::close)
    }

    private fun tryAndLog(block: () -> Unit) {
        try {
            block()
        } catch (err: Exception) {
            LOGGER.error(err.message, err)
        }
    }

    private fun shutdownHook() {
        LOGGER.info("received shutdown signal, stopping app")
        stop()
    }
}
