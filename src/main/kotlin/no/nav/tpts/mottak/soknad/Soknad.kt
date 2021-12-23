package no.nav.tpts.mottak.soknad

import kotlinx.serialization.Serializable
import no.nav.tpts.mottak.util.LocalDateSerializer
import no.nav.tpts.mottak.util.LocalDateTimeSerializer
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
data class Soknad(
    val navn: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val opprettetDato: LocalDateTime,
    @Serializable(with = LocalDateSerializer::class)
    val brukerStartDato: LocalDate?,
    @Serializable(with = LocalDateSerializer::class)
    val brukerSluttDato: LocalDate?
) {
    companion object
}
