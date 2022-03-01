package no.nav.tpts.mottak

private fun getPropertyValueByEnvironment(devValue: String, prodValue: String): String {
    return when (System.getenv("NAIS_CLUSTER_NAME")) {
        "dev-gcp" -> devValue
        "prod-gcp" -> prodValue
        else -> devValue
    }
}

fun getSafUrl(): String = getPropertyValueByEnvironment(
    devValue = "https://saf.dev-fss-pub.nais.io",
    prodValue = "https://saf.prod-fss-pub.nais.io"
)

fun topicName(): String = getPropertyValueByEnvironment(
    devValue = "teamdokumenthandtering.aapen-dok-journalfoering-q1",
    prodValue = "teamdokumenthandtering.aapen-dok-journalfoering"
)

object AuthConfig {
    val issuer = System.getenv("AZURE_ISSUER")
    val jwksUri = System.getenv("AZURE_JWKS_URI")
    val clientId = System.getenv("AZURE_APP_CLIENT_ID")
}

private val arenaClientId = System.getenv("TPTS_ARENA_CLIENT_ID")
private val arenaScope = "api://$arenaClientId/.default"
private const val SAF_CLIENT_NAME = "dev-fss.teamdokumenthandtering.saf"
private const val SAF_SCOPE = "api://$SAF_CLIENT_NAME/.default"

enum class Scope(val value: String) {
    ARENA(arenaScope),
    SAF(SAF_SCOPE)
}
