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
    @Serializable(with = LocalDateSerializer::class) val brukerRegistrertStartDato: LocalDate?,
    @Serializable(with = LocalDateSerializer::class) val brukerRegistrertSluttDato: LocalDate?,
    @Serializable(with = LocalDateSerializer::class) val systemRegistrertStartDato: LocalDate?,
    @Serializable(with = LocalDateSerializer::class) val systemRegistrertSluttDato: LocalDate?
) {
    companion object {
        fun fromJoarkSoknad(joarkSoknad: JoarkSoknad): Soknad {
            val personalia = joarkSoknad.fakta.first { it.key == "personalia" }
            val fnr = personalia.properties?.fnr
                ?: throw IllegalArgumentException("No matching fnr, cannot behandle ${joarkSoknad.soknadId}")
            val valgtTiltak = joarkSoknad.fakta.firstOrNull { it.key == "tiltaksliste.valgtTiltak" }
            val tiltaksInfoBruker = joarkSoknad.fakta.firstOrNull { it.key == "tiltaksliste.annetTiltak" }
            val tiltaksInfoSystem = joarkSoknad.fakta.firstOrNull { it.faktumId.toString() == valgtTiltak?.value }
            return Soknad(
                id = joarkSoknad.soknadId.toString(),
                fornavn = personalia.properties.fornavn,
                etternavn = personalia.properties.etternavn,
                ident = fnr,
                opprettet = LocalDateTime.MIN,
                brukerRegistrertStartDato = tiltaksInfoBruker?.properties?.fom,
                brukerRegistrertSluttDato = tiltaksInfoBruker?.properties?.tom,
                systemRegistrertStartDato = tiltaksInfoSystem?.properties?.startdato,
                systemRegistrertSluttDato = tiltaksInfoSystem?.properties?.sluttdato
            )
        }
    }
}
