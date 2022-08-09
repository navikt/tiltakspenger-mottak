package no.nav.tiltakspenger.mottak.søknad.søknadList

import kotlinx.serialization.Serializable

@Serializable
data class Barnetillegg(
    val fornavn: String?,
    val etternavn: String?,
    val alder: Int,
    val ident: String,
    val bosted: String
)
