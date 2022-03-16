package no.nav.tpts.mottak.soknad.soknadList

import kotlinx.serialization.Serializable
import no.nav.tpts.mottak.databind.LocalDateSerializer
import no.nav.tpts.mottak.databind.LocalDateTimeSerializer
import no.nav.tpts.mottak.joark.models.JoarkSoknad
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
data class Soknad(
    val id: String,
    val fornavn: String?,
    val etternavn: String,
    val sokerId: Int,
    @Serializable(with = LocalDateTimeSerializer::class) val opprettet: LocalDateTime,
    @Serializable(with = LocalDateSerializer::class) val brukerStartDato: LocalDate?,
    @Serializable(with = LocalDateSerializer::class) val brukerSluttDato: LocalDate?
) {
    companion object {
        fun fromJoarkSoknad(joarkSoknad: JoarkSoknad): Soknad {
            return Soknad(
                id = joarkSoknad.soknadId.toString(),
                fornavn = "",
                etternavn = "",
                sokerId = 0,
                opprettet = LocalDateTime.MIN,
                brukerStartDato = LocalDate.MAX,
                brukerSluttDato = LocalDate.MAX
            )
        }
    }
}
