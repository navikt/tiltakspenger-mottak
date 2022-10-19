package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.mottak.serder.LenientLocalDateSerializer
import java.time.LocalDate

@Serializable
data class TrygdOgPensjon(
    val utbetaler: String,
    val prosent: Int?,
    @Serializable(with = LenientLocalDateSerializer::class)
    val fom: LocalDate?,
    @Serializable(with = LenientLocalDateSerializer::class)
    val tom: LocalDate?
)
