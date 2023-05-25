package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.mottak.serder.StrictLocalDateSerializer
import java.time.LocalDate

@Serializable
data class BarnetilleggDTO(
    @Serializable(with = StrictLocalDateSerializer::class)
    val fødselsdato: LocalDate? = null,
    val fornavn: String? = null,
    val mellomnavn: String? = null,
    val etternavn: String? = null,
    val oppholderSegIEØS: JaNeiSpmDTO,
)
