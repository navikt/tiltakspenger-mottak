package no.nav.tpts.mottak.soknad.soknadList

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.nav.tpts.mottak.databind.LocalDateSerializer
import no.nav.tpts.mottak.databind.LocalDateTimeSerializer
import no.nav.tpts.mottak.joark.models.JoarkSoknad
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
data class Soknad(
    val id: String,
    val fornavn: String?,
    val etternavn: String?,
    val ident: String,
    val onKvp: Boolean?,
    val onIntroduksjonsprogrammet: Boolean?,
    @Serializable(with = LocalDateTimeSerializer::class)
    val opprettet: LocalDateTime?,
    @Serializable(with = LocalDateSerializer::class)
    val brukerRegistrertStartDato: LocalDate?,
    @Serializable(with = LocalDateSerializer::class)
    val brukerRegistrertSluttDato: LocalDate?,
    @Serializable(with = LocalDateSerializer::class)
    val systemRegistrertStartDato: LocalDate?,
    @Serializable(with = LocalDateSerializer::class)
    val systemRegistrertSluttDato: LocalDate?
) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }

        fun fromJson(json: String): Soknad {
            val joarkSoknad = this.json.decodeFromString<JoarkSoknad>(json)
            val personalia = joarkSoknad.fakta.firstOrNull { it.key == "personalia" }
            val fnr = personalia?.properties?.fnr
            requireNotNull(fnr) { "No fnr, cannot handle soknad with id ${joarkSoknad.soknadId}" }
            val valgtTiltak = joarkSoknad.fakta.firstOrNull { it.key == "tiltaksliste.valgtTiltak" }
            val tiltaksInfoBruker = joarkSoknad.fakta.firstOrNull { it.key == "tiltaksliste.annetTiltak" }
            val tiltaksInfoSystem = joarkSoknad.fakta.firstOrNull { it.faktumId.toString() == valgtTiltak?.value }
            val onKvp =
                joarkSoknad.fakta.firstOrNull { it.key == "informasjonsside.kvalifiseringsprogram" }?.value === "ja"
            val onIntroduksjonsprogrammet =
                joarkSoknad.fakta.firstOrNull { it.key == "informasjonsside.deltarIIntroprogram" }?.value == "ja"
            return Soknad(
                id = joarkSoknad.soknadId.toString(),
                fornavn = personalia.properties.fornavn,
                etternavn = personalia.properties.etternavn,
                ident = fnr,
                opprettet = joarkSoknad.opprettetDato,
                brukerRegistrertStartDato = tiltaksInfoBruker?.properties?.fom,
                brukerRegistrertSluttDato = tiltaksInfoBruker?.properties?.tom,
                systemRegistrertStartDato = tiltaksInfoSystem?.properties?.startdato,
                systemRegistrertSluttDato = tiltaksInfoSystem?.properties?.sluttdato,
                onKvp = onKvp,
                onIntroduksjonsprogrammet = onIntroduksjonsprogrammet
            )
        }
    }
}
