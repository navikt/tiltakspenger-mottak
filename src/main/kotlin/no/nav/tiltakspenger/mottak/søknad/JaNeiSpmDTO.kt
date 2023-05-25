package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable

@Serializable
data class JaNeiSpmDTO(
    val svar: SpmSvarDTO,
)
