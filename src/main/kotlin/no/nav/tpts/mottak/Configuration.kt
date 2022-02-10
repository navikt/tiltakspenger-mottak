package no.nav.tpts.mottak

private fun getPropertyValueByEnvironment(devValue: String, prodValue: String): String {
    return when (System.getenv("NAIS_CLUSTER_NAME")) {
        "dev-gcp" -> devValue
        "prod-gcp" -> prodValue
        else -> devValue
    }
}

fun getSafUrl(): String = getPropertyValueByEnvironment(
    devValue = "saf.dev-fss-pub.nais.io",
    prodValue = "saf.prod-fss-pub.nais.io"
)

fun topicName(): String = getPropertyValueByEnvironment(
    devValue = "teamdokumenthandtering.aapen-dok-journalfoering-q1",
    prodValue = "teamdokumenthandtering.aapen-dok-journalfoering"
)

object AuthConfig {
    val issuer = System.getenv("AZURE_ISSUER")
    val jwksUri = System.getenv("AZURE_JWKS_URI")
}
