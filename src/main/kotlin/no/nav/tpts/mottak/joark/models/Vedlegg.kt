package no.nav.tpts.mottak.joark.models

import kotlinx.serialization.Serializable

@Serializable
data class Vedlegg(
    val vedleggId: Int,
    val soknadId: Int,
    val faktumId: Int?,
    val skjemaNummer: String,
    val skjemanummerTillegg: String,
    val innsendingsvalg: String,
    val opprinneligInnsendingsvalg: String?,
    val navn: String?,
    val storrelse: Int,
    val opprettetDato: Long,
    val fillagerReferanse: String,
    val urls: Map<String, String>,
    val tittel: String?,
    val aarsak: String,
    val filnavn: String?,
    val mimetype: String?
)
