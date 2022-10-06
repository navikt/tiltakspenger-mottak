package no.nav.tiltakspenger.mottak

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.intType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import io.getunleash.DefaultUnleash
import io.getunleash.strategy.Strategy
import io.getunleash.util.UnleashConfig
import java.net.URL

enum class Profile {
    LOCAL, DEV, PROD
}

object Configuration {
    private val kafka = mapOf(
        "KAFKA_RAPID_TOPIC" to "tpts.rapid.v1",
        "KAFKA_RESET_POLICY" to "earliest",  // latest?
        "KAFKA_BROKERS" to System.getenv("KAFKA_BROKERS"),
        "KAFKA_KEYSTORE_PATH" to System.getenv("KAFKA_KEYSTORE_PATH"),
        "KAFKA_TRUSTSTORE_PATH" to System.getenv("KAFKA_TRUSTSTORE_PATH"),
        "KAFKA_SCHEMA_REGISTRY" to System.getenv("KAFKA_SCHEMA_REGISTRY"),
        "KAFKA_SCHEMA_REGISTRY_USER" to System.getenv("KAFKA_SCHEMA_REGISTRY_USER"),
        "KAFKA_SCHEMA_REGISTRY_PASSWORD" to System.getenv("KAFKA_SCHEMA_REGISTRY_PASSWORD"),
        "KAFKA_CREDSTORE_PASSWORD" to System.getenv("KAFKA_CREDSTORE_PASSWORD"),
    )
    private val otherDefaultProperties = mapOf(
        "application.httpPort" to 8080.toString(),
        "AZURE_APP_CLIENT_ID" to System.getenv("AZURE_APP_CLIENT_ID"),
        "AZURE_APP_CLIENT_SECRET" to System.getenv("AZURE_APP_CLIENT_SECRET"),
        "AZURE_APP_WELL_KNOWN_URL" to System.getenv("AZURE_APP_WELL_KNOWN_URL"),
    )
    private val defaultProperties = ConfigurationMap(kafka + otherDefaultProperties)
    private val localProperties = ConfigurationMap(
        mapOf(
            "tptsRapidName" to "tpts.rapid.v1",
            "KAFKA_CONSUMER_GROUP_ID" to "consumer-v1",
            "application.profile" to Profile.LOCAL.toString(),
            "joarkTopicName" to "joark.local",
            "KAFKA_BROKERS" to "KAFKA_BROKERS",
            "KAFKA_KEYSTORE_PATH" to "KAFKA_KEYSTORE_PATH",
            "KAFKA_TRUSTSTORE_PATH" to "KAFKA_TRUSTSTORE_PATH",
            "KAFKA_SCHEMA_REGISTRY" to "KAFKA_SCHEMA_REGISTRY",
            "KAFKA_SCHEMA_REGISTRY_USER" to "KAFKA_SCHEMA_REGISTRY_USER",
            "KAFKA_SCHEMA_REGISTRY_PASSWORD" to "KAFKA_SCHEMA_REGISTRY_PASSWORD",
            "KAFKA_CREDSTORE_PASSWORD" to "KAFKA_CREDSTORE_PASSWORD",
            "safBaseUrl" to "https://localhost:8080",
            "safScope" to "api://localhost:/.default",
        )
    )
    private val devProperties = ConfigurationMap(
        mapOf(
            "tptsRapidName" to "tpts.rapid.v1",
            "KAFKA_CONSUMER_GROUP_ID" to "tiltakspenger-aiven-mottak-v2",
            "application.profile" to Profile.DEV.toString(),
            "safBaseUrl" to "https://saf.dev-fss-pub.nais.io",
            "safScope" to "api://dev-fss.teamdokumenthandtering.saf/.default",
            "joarkTopicName" to "teamdokumenthandtering.aapen-dok-journalfoering-q1"
        )
    )
    private val prodProperties = ConfigurationMap(
        mapOf(
            "tptsRapidName" to "tpts.rapid.v1",
            "KAFKA_CONSUMER_GROUP_ID" to "tiltakspenger-aiven-mottak-v2",
            "application.profile" to Profile.PROD.toString(),
            "safBaseUrl" to "https://saf.prod-fss-pub.nais.io",
            "safScope" to "api://prod-fss.teamdokumenthandtering.saf/.default",
            "joarkTopicName" to "teamdokumenthandtering.aapen-dok-journalfoering"
        )
    )

    private fun config() = when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
        "dev-gcp" ->
            systemProperties() overriding EnvironmentVariables overriding devProperties overriding defaultProperties

        "prod-gcp" ->
            systemProperties() overriding EnvironmentVariables overriding prodProperties overriding defaultProperties

        else -> {
            systemProperties() overriding EnvironmentVariables overriding localProperties overriding defaultProperties
        }
    }

    data class OauthConfig(
        val scope: String = config()[Key("safScope", stringType)],
        val clientId: String = config()[Key("AZURE_APP_CLIENT_ID", stringType)],
        val clientSecret: String = config()[Key("AZURE_APP_CLIENT_SECRET", stringType)],
        val wellknownUrl: String = config()[Key("AZURE_APP_WELL_KNOWN_URL", stringType)]
    )

    data class KafkaConfig(
        val joarkTopic: String = config()[Key("joarkTopicName", stringType)],
        val rapidTopic: String = config()[Key("KAFKA_RAPID_TOPIC", stringType)],
        val resetPolicy: String = config()[Key("KAFKA_RESET_POLICY", stringType)],
        val consumerGroupId: String = config()[Key("KAFKA_CONSUMER_GROUP_ID", stringType)],
        val brokers: String = config()[Key("KAFKA_BROKERS", stringType)],
        val keystorePath: String = config()[Key("KAFKA_KEYSTORE_PATH", stringType)],
        val truststorePath: String = config()[Key("KAFKA_TRUSTSTORE_PATH", stringType)],
        val schemaRegistry: String = config()[Key("KAFKA_SCHEMA_REGISTRY", stringType)],
        val schemaRegistryUser: String = config()[Key("KAFKA_SCHEMA_REGISTRY_USER", stringType)],
        val schemaRegistryPassword: String = config()[Key("KAFKA_SCHEMA_REGISTRY_PASSWORD", stringType)],
        val credstorePassword: String = config()[Key("KAFKA_CREDSTORE_PASSWORD", stringType)],
        val maxPollRecords: Int = 5,
        val maxPollIntervalMs: Int = 300_000
    )

    @JvmInline
    value class SafConfig(val baseUrl: String = config()[Key("safBaseUrl", stringType)]) {
        init {
            check(URL(baseUrl).toString() == baseUrl)
        }
    }

    fun tptsRapidName(): String = config()[Key("tptsRapidName", stringType)]

    fun applicationPort(): Int = config()[Key("application.httpPort", intType)]
}

val unleash by lazy {
    DefaultUnleash(
        UnleashConfig.builder()
            .appName(requireNotNull(System.getenv("NAIS_APP_NAME")) { "Expected NAIS_APP_NAME" })
            .instanceId(requireNotNull(System.getenv("HOSTNAME")) { "Expected HOSTNAME" })
            .environment(requireNotNull(System.getenv("NAIS_CLUSTER_NAME")) { "Expected NAIS_CLUSTER_NAME" })
            .unleashAPI("https://unleash.nais.io/api/")
            .build(), ByClusterStrategy(System.getenv("NAIS_CLUSTER_NAME"))
    )
}

class ByClusterStrategy(private val cluster: String) : Strategy {
    override fun getName(): String = "byCluster"

    override fun isEnabled(parameters: Map<String, String>) =
        if (parameters["cluster"] == null) false else parameters["cluster"]!!.contains(cluster, ignoreCase = true)
}
