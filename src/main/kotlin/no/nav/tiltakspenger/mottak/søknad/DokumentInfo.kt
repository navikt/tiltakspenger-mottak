package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable

@Serializable
data class DokumentInfo(
    val journalpostId: String,
    val dokumentInfoId: String,
    val filnavn: String?,
)
