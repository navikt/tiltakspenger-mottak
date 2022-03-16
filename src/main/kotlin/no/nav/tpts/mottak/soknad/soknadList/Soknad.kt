package no.nav.tpts.mottak.soknad.soknadList

import kotlinx.serialization.Serializable
import no.nav.tpts.mottak.databind.LocalDateSerializer
import no.nav.tpts.mottak.databind.LocalDateTimeSerializer
import no.nav.tpts.mottak.joark.models.JoarkSoknad
import java.lang.IllegalArgumentException
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
data class Soknad(
        val id: String,
        val fornavn: String?,
        val etternavn: String?,
        val ident: String,
        @Serializable(with = LocalDateTimeSerializer::class) val opprettet: LocalDateTime,
        @Serializable(with = LocalDateSerializer::class) val brukerStartDato: LocalDate?,
        @Serializable(with = LocalDateSerializer::class) val brukerSluttDato: LocalDate?,
        @Serializable(with = LocalDateSerializer::class) val systemStartDato: LocalDate?,
        @Serializable(with = LocalDateSerializer::class) val systemSluttDato: LocalDate?
) {
    companion object {
        fun fromJoarkSoknad(joarkSoknad: JoarkSoknad): Soknad {
            val personalia = joarkSoknad.fakta.first { it.key == "personalia" }
            val fnr = personalia.properties?.fnr
                ?: throw IllegalArgumentException("No matching fnr, cannot behandle ${joarkSoknad.soknadId}")
            // Hvis valgtTiltak =="annetTiltak"
            // Hvis tiltak fra arena kun startdato
            val valgtTiltak = joarkSoknad.fakta.first { it.key === "tiltaksliste.valgtTiltak" }
            val valgtIsFromSystem = valgtTiltak.value === "tiltaksliste.annetTiltak"
            val tiltaksInfoBruker = joarkSoknad.fakta.firstOrNull { it.key === "tiltaksliste.annetTiltak" }
            val tiltaksInfoSystem = if (valgtIsFromSystem) joarkSoknad.fakta.firstOrNull { it.key === "" } else null


            return Soknad(
                id = joarkSoknad.soknadId.toString(),
                fornavn = personalia.properties.fornavn,
                etternavn = personalia.properties.etternavn,
                ident = fnr,
                opprettet = LocalDateTime.MIN,
                brukerStartDato = tiltaksInfoBruker?.properties?.fom,
                brukerSluttDato = tiltaksInfoBruker?.properties?.tom,
                systemStartDato = tiltaksInfoSystem?.properties?.startdato,
                systemSluttDato = tiltaksInfoSystem?.properties?.sluttdato
            )
        }


    }
}
