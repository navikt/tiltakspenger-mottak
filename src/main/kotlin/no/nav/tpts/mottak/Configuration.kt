package no.nav.tpts.mottak

fun getUrl(devUrl: String, prodUrl: String): String {

    return when (System.getenv("NAIS_CLUSTER_NAME")) {
        "dev-gcp" -> devUrl
        "prod-gcp" -> prodUrl
        else -> devUrl
    }
}

fun getSafUrl(): String {
    return getUrl(
        "https://saf.dev-fss-pub.nais.io",
        "https://saf.prod-fss-pub.nais.io"
    )
}

fun topicName(): String {
    return getUrl(
        "teamdokumenthandtering.aapen-dok-journalfoering-q1",
        "teamdokumenthandtering.aapen-dok-journalfoering"
    )
}
