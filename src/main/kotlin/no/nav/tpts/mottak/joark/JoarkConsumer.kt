package no.nav.tpts.mottak.joark

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import java.io.File
import java.time.Duration
import java.util.Properties

const val MAX_POLL_RECORDS = 50
const val SIXTY = 60
val maxPollIntervalMs = Duration.ofSeconds(SIXTY + MAX_POLL_RECORDS * 2.toLong()).toMillis()
val username: String = System.getenv("")
val password: String = System.getenv("")

fun createJoarkConsumer(): Consumer<String, GenericRecord> {
    return KafkaConsumer(
        Properties().also {
            it[ConsumerConfig.GROUP_ID_CONFIG] = "tpts-tiltakspenger-mottak-v1"
            it[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
            it[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
            it[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaAvroDeserializer::class.java
            it[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "false"
            it[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = MAX_POLL_RECORDS
            it[ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG] = "$maxPollIntervalMs"
            it[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SASL_PLAINTEXT"
            it[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = System.getenv("KAFKA_BROKERS")
            it[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SASL_SSL"
            it[SaslConfigs.SASL_MECHANISM] = "PLAIN"
            it[SaslConfigs.SASL_JAAS_CONFIG] = "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                    "username=\"$username\" password=\"$password\""
            it[AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG] = System.getenv("KAFKA_SCHEMA_REGISTRY")
            it[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = File(System.getenv("NAV_TRUSTSTORE_PATH")).absolutePath
            it[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = System.getenv("NAV_TRUSTSTORE_PASSWORD")
        }
    )
}

fun subscribeToTopic() {
    throw NotImplementedError()
}
