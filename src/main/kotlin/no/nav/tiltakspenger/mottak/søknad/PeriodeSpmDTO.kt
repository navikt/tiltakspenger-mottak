package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.mottak.serder.StrictLocalDateSerializer
import java.time.LocalDate

@Serializable
data class PeriodeSpmDTO(
    val svar: SpmSvarDTO,
    @Serializable(with = StrictLocalDateSerializer::class)
    val fom: LocalDate?,
    @Serializable(with = StrictLocalDateSerializer::class)
    val tom: LocalDate?,
)
