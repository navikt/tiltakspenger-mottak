package no.nav.tiltakspenger.mottak

const val TPTS_RAPID_NAME = "tpts.rapid.v1"

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

fun joarkTopicName(): String = getPropertyValueByEnvironment(
    devValue = "teamdokumenthandtering.aapen-dok-journalfoering-q1",
    prodValue = "teamdokumenthandtering.aapen-dok-journalfoering"
)
