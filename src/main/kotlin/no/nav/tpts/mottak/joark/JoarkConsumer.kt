package no.nav.tpts.mottak.joark

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.security.auth.SecurityProtocol
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.Properties

const val MAX_POLL_RECORDS = 50
const val SIXTY = 60
val maxPollIntervalMs = Duration.ofSeconds(SIXTY + MAX_POLL_RECORDS * 2.toLong()).toMillis()

fun createJoarkConsumer(topicName: String): KafkaConsumer<String, GenericRecord> {
    return KafkaConsumer<String, GenericRecord>(
        Properties().also {
            it[ConsumerConfig.GROUP_ID_CONFIG] = "tpts-tiltakspenger-aiven-mottak-v1"
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
