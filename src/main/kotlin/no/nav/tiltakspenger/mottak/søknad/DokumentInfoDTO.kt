package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable

@Serializable
data class DokumentInfoDTO(
    val journalpostId: String,
    val dokumentInfoId: String,
    val filnavn: String?,
)
