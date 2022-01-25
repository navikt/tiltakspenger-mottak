package no.nav.tpts.mottak.soknad

import kotlinx.serialization.Serializable
import no.nav.tpts.mottak.databind.LocalDateSerializer
import no.nav.tpts.mottak.databind.LocalDateTimeSerializer
import java.time.LocalDate
import java.time.LocalDateTime

enum class SoknadStatus {
    AVSLAG,
    IKKE_BEHANDLET,
    BEHANDLES
}

@Serializable
data class Soknad(
    val id: String,
    val fornavn: String,
    val etternavn: String,
    val identer: List<String>,
    val soknadStatus: SoknadStatus = SoknadStatus.IKKE_BEHANDLET,
    @Serializable(with = LocalDateTimeSerializer::class) val opprettetDato: LocalDateTime,
    @Serializable(with = LocalDateSerializer::class) val brukerStartDato: LocalDate?,
    @Serializable(with = LocalDateSerializer::class) val brukerSluttDato: LocalDate?
)
