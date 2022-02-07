package no.nav.tpts.mottak

fun topicName(): String {
    val clusterName = System.getenv("NAIS_CLUSTER_NAME")
    LOG.info { "clusterName: $clusterName" }
    return when (clusterName) {
        "dev-gcp" -> "teamdokumenthandtering.aapen-dok-journalfoering-q1"
        "prod-gcp" -> "teamdokumenthandtering.aapen-dok-journalfoering"
        else -> "teamdokumenthandtering.aapen-dok-journalfoering-q1"
    }
}

object AuthConfig {
    val issuer = System.getenv("AZURE_ISSUER")
    val jwksUri = System.getenv("AZURE_JWKS_URI")
}
