package no.nav.tiltakspenger.mottak.søknad

import com.fasterxml.jackson.annotation.JsonRawValue

data class Søknad(
    val ident: String,
    val hoveddokument: DokumentInfo,
    val versjon: String,
    @JsonRawValue
    val søknad: String,
    val vedlegg: List<DokumentInfo>,
)
