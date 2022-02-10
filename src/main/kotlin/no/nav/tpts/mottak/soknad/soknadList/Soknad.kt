package no.nav.tpts.mottak.soknad.soknadList

import kotlinx.serialization.Serializable
import no.nav.tpts.mottak.databind.LocalDateSerializer
import no.nav.tpts.mottak.databind.LocalDateTimeSerializer
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
data class Soknad(
    val id: String,
    val fornavn: String?,
    val etternavn: String,
    val ident: String,
    @Serializable(with = LocalDateTimeSerializer::class) val opprettet: LocalDateTime,
    @Serializable(with = LocalDateSerializer::class) val brukerStartDato: LocalDate?,
    @Serializable(with = LocalDateSerializer::class) val brukerSluttDato: LocalDate?
)
