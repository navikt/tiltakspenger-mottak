package no.nav.tiltakspenger.mottak.soknad.soknadList

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.nav.tiltakspenger.mottak.databind.LocalDateSerializer
import no.nav.tiltakspenger.mottak.databind.LocalDateTimeSerializer
import no.nav.tiltakspenger.mottak.joark.models.JoarkSoknad
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
data class Soknad(
    val id: String,
    val fornavn: String?,
    val etternavn: String?,
    val ident: String,
    val deltarKvp: Boolean,
    val deltarIntroduksjonsprogrammet: Boolean?,
    val oppholdInstitusjon: Boolean?,
    val typeInstitusjon: String?,
    @Serializable val tiltaksArrangoer: String?,
    @Serializable val tiltaksType: String?,
    @Serializable(with = LocalDateTimeSerializer::class)
    val opprettet: LocalDateTime?,
    @Serializable(with = LocalDateSerializer::class)
    val brukerRegistrertStartDato: LocalDate?,
    @Serializable(with = LocalDateSerializer::class)
    val brukerRegistrertSluttDato: LocalDate?,
    @Serializable(with = LocalDateSerializer::class)
    val systemRegistrertStartDato: LocalDate?,
    @Serializable(with = LocalDateSerializer::class)
    val systemRegistrertSluttDato: LocalDate?,
    val barnetillegg: List<Barnetillegg>
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
            val deltarKvp =
                joarkSoknad.fakta.firstOrNull { it.key == "informasjonsside.kvalifiseringsprogram" }?.value == "ja"
            /* Faktum "informasjonsside.deltarIIntroprogram" gir strengen "false" når deltaker svarer ja på deltakelse
            * og null når søker svarer nei, sjekker derfor kommune istedet for å unngå (mer) forvirring */
            val deltarIntroduksjonsprogrammet =
                joarkSoknad.fakta.firstOrNull { it.key == "informasjonsside.deltarIIntroprogram.info" }
                    ?.properties?.kommune?.isNotEmpty() ?: false
            val oppholdInstitusjon =
                joarkSoknad.fakta.firstOrNull { it.key == "informasjonsside.institusjon" }?.value == "ja"
            val typeInstitusjon = if (oppholdInstitusjon)
                joarkSoknad.fakta.firstOrNull { it.key == "informasjonsside.institusjon.ja.hvaslags" }?.value else null
            val tiltaksArrangoer =
                tiltaksInfoBruker?.properties?.arrangoernavn ?: tiltaksInfoSystem?.properties?.arrangoer
            val tiltaksType = tiltaksInfoBruker?.value ?: tiltaksInfoSystem?.properties?.navn
            val barneFakta = joarkSoknad.fakta
                .filter { it.key == "barn" }
            val barneTillegg = barneFakta
                .filter { it.properties?.sokerbarnetillegg?.value == true }
                .filter { it.properties?.fnr?.isNotEmpty() ?: false }
                .map {
                    Barnetillegg(
                        ident = it.properties?.fnr!!,
                        fornavn = it.properties.fornavn,
                        etternavn = it.properties.etternavn,
                        alder = it.properties.alder!!.toInt(),
                        bosted = it.properties.land!!
                    )
                }

            return Soknad(
                id = joarkSoknad.soknadId.toString(),
                fornavn = personalia.properties.fornavn,
                etternavn = personalia.properties.etternavn,
                ident = fnr,
                deltarKvp = deltarKvp,
                deltarIntroduksjonsprogrammet = deltarIntroduksjonsprogrammet,
                oppholdInstitusjon = oppholdInstitusjon,
                tiltaksArrangoer = tiltaksArrangoer,
                tiltaksType = tiltaksType,
                typeInstitusjon = typeInstitusjon,
                opprettet = joarkSoknad.opprettetDato,
                brukerRegistrertStartDato = tiltaksInfoBruker?.properties?.fom,
                brukerRegistrertSluttDato = tiltaksInfoBruker?.properties?.tom,
                systemRegistrertStartDato = tiltaksInfoSystem?.properties?.startdato,
                systemRegistrertSluttDato = tiltaksInfoSystem?.properties?.sluttdato,
                barnetillegg = barneTillegg
            )
        }
    }
}
