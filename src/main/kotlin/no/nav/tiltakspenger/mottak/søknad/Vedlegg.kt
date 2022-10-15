package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable

@Serializable
data class Vedlegg(
    val journalpostId: String,
    val dokumentInfoId: String,
    val filnavn: String?,
)
