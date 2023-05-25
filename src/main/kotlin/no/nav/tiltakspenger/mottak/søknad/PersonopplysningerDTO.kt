package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable

@Serializable
data class PersonopplysningerDTO(
    val ident: String,
    val fornavn: String,
    val etternavn: String,
)
