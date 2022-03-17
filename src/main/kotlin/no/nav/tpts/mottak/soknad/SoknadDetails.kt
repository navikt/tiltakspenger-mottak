package no.nav.tpts.mottak.soknad

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
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
    @Serializable(with = LocalDateTimeSerializer::class) val opprettet: LocalDateTime? = null,
    @Serializable(with = LocalDateSerializer::class) val brukerSluttDato: LocalDate? = null,
    @Serializable(with = LocalDateSerializer::class) val brukerStartDato: LocalDate? = null,
) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }

        fun fromJson(json: String): SoknadDetails {
            val joarkSoknad = this.json.decodeFromString<JoarkSoknad>(json)
            val personalia = joarkSoknad.fakta.firstOrNull { it.key == "personalia" }?.properties
            val fnr = personalia?.fnr
            requireNotNull(fnr) { "No fnr, cannot handle soknad with id ${joarkSoknad.soknadId}" }
            return SoknadDetails(
                soknadId = joarkSoknad.soknadId.toString(),
                opprettet = joarkSoknad.opprettetDato,
                fornavn = personalia.fornavn,
                etternavn = personalia.etternavn,
                fnr = fnr,
                tiltak = Tiltak.fromJoarkSoknad(joarkSoknad)
            )
        }
    }
}
