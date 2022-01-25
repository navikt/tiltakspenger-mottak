package no.nav.tpts.mottak.soknad

import kotlinx.serialization.Serializable
import no.nav.tpts.mottak.databind.LocalDateSerializer
import no.nav.tpts.mottak.databind.LocalDateTimeSerializer
import no.nav.tpts.mottak.joark.models.JoarkSoknad
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
data class SoknadDetails(
    val soknadId: String? = null,
    val fornavn: String? = null,
    val fnr: String? = null,
    val etternavn: String? = null,
    val tiltak: Tiltak? = null,
    val soknadStatus: SoknadStatus = SoknadStatus.IKKE_BEHANDLET,
    @Serializable(with = LocalDateTimeSerializer::class) val opprettetDato: LocalDateTime? = null,
    @Serializable(with = LocalDateSerializer::class) val brukerSluttDato: LocalDate? = null,
    @Serializable(with = LocalDateSerializer::class) val brukerStartDato: LocalDate? = null,
) {
    companion object {
        fun fromJoarkSoknad(joarkSoknad: JoarkSoknad): SoknadDetails {
            val personalia = joarkSoknad.fakta.firstOrNull { it.key == "personalia" }?.properties
            return SoknadDetails(
                soknadId = joarkSoknad.soknadId.toString(),
                opprettetDato = joarkSoknad.opprettetDato,
                fornavn = personalia?.fornavn,
                etternavn = personalia?.etternavn,
                fnr = personalia?.fnr,
                tiltak = Tiltak.fromJoarkSoknad(joarkSoknad)
            )
        }
    }
}
