package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.mottak.serder.LocalDateSerializer
import java.time.LocalDate

@Serializable
data class IntroduksjonsprogrammetDetaljer(
    @Serializable(with = LocalDateSerializer::class) val fom: LocalDate,
    @Serializable(with = LocalDateSerializer::class) val tom: LocalDate? = null,
)
