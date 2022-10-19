package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.mottak.serder.StrictLocalDateSerializer
import java.time.LocalDate

@Serializable
data class TrygdOgPensjon(
    val utbetaler: String,
    val prosent: Int?,
    @Serializable(with = StrictLocalDateSerializer::class)
    val fom: LocalDate?,
    @Serializable(with = StrictLocalDateSerializer::class)
    val tom: LocalDate?
)
