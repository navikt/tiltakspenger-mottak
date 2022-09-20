package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.nav.tiltakspenger.mottak.databind.LocalDateTimeSerializer
import no.nav.tiltakspenger.mottak.joark.models.JoarkSøknad
import java.time.LocalDateTime

@Serializable
data class Søknad(
    val søknadId: String,
    val journalpostId: String,
    val dokumentInfoId: String,
    val fornavn: String?,
    val etternavn: String?,
    val ident: String,
    val deltarKvp: Boolean,
    val deltarIntroduksjonsprogrammet: Boolean?,
    val oppholdInstitusjon: Boolean,
    val typeInstitusjon: String?,
    @Serializable(with = LocalDateTimeSerializer::class)
    val opprettet: LocalDateTime,
    val barnetillegg: List<Barnetillegg>,
    val arenaTiltak: ArenaTiltak?,
    val brukerregistrertTiltak: BrukerregistrertTiltak?,
    val trygdOgPensjon: List<TrygdOgPensjon>,
    val fritekst: String? = null,
) {

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }

        fun fromJson(json: String, journalpostId: String, dokumentInfoId: String): Søknad {
            val joarkSøknad = Companion.json.decodeFromString<JoarkSøknad>(json)
            val personalia = joarkSøknad.fakta.firstOrNull { it.key == "personalia" }
            val fnr = personalia?.properties?.fnr
            requireNotNull(fnr) { "No fnr, cannot handle soknad with id ${joarkSøknad.soknadId}" }
            val deltarKvp =
                joarkSøknad.fakta.firstOrNull { it.key == "informasjonsside.kvalifiseringsprogram" }?.value == "ja"
            /* Faktum "informasjonsside.deltarIIntroprogram" gir strengen "false" når deltaker svarer ja på deltakelse
            * og null når søker svarer nei, sjekker derfor kommune istedet for å unngå (mer) forvirring */
            val deltarIntroduksjonsprogrammet =
                joarkSøknad.fakta.firstOrNull { it.key == "informasjonsside.deltarIIntroprogram.info" }
                    ?.properties?.kommune?.isNotEmpty() ?: false
            val oppholdInstitusjon =
                joarkSøknad.fakta.first { it.key == "informasjonsside.institusjon" }.value == "ja"
            val typeInstitusjon = joarkSøknad.fakta
                .firstOrNull { it.key == "informasjonsside.institusjon.ja.hvaslags" }?.value
            val arenaTiltak = ArenaTiltak.fromJoarkSoknad(joarkSøknad)
            val brukerregistrertTiltak = BrukerregistrertTiltak.fromJoarkSoknad(joarkSøknad)
            val barneTillegg = joarkSøknad.fakta
                .filter { it.key == "barn" }
                .filter { it.properties.sokerbarnetillegg ?: false }
                .map {
                    Barnetillegg(
                        ident = it.properties.fnr,
                        fødselsdato = it.properties.fodselsdato,
                        alder = it.properties.alder!!.toInt(),
                        land = it.properties.land!!,
                        fornavn = it.properties.fornavn,
                        etternavn = it.properties.etternavn,
                    )
                }
            val trygdOgPensjon = joarkSøknad.fakta
                .filter { it.key == "trygdogpensjon.utbetalere" && it.properties.utbetaler != null }
                .map {
                    TrygdOgPensjon(
                        utbetaler = it.properties.utbetaler!!,
                        prosent = it.properties.prosent,
                        fom = it.properties.fom!!,
                        tom = it.properties.tom
                    )
                }
            val fritekst = joarkSøknad.fakta.firstOrNull { it.key == "tilleggsopplysninger.fritekst" }?.value

            return Søknad(
                søknadId = joarkSøknad.soknadId.toString(),
                journalpostId = journalpostId,
                dokumentInfoId = dokumentInfoId,
                fornavn = personalia.properties.fornavn,
                etternavn = personalia.properties.etternavn,
                ident = fnr,
                deltarKvp = deltarKvp,
                deltarIntroduksjonsprogrammet = deltarIntroduksjonsprogrammet,
                oppholdInstitusjon = oppholdInstitusjon,
                typeInstitusjon = typeInstitusjon,
                opprettet = joarkSøknad.opprettetDato,
                barnetillegg = barneTillegg,
                arenaTiltak = arenaTiltak,
                brukerregistrertTiltak = brukerregistrertTiltak,
                trygdOgPensjon = trygdOgPensjon,
                fritekst = fritekst,
            )
        }
    }
}
