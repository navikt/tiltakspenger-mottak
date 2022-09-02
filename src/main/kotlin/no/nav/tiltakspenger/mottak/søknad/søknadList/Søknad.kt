package no.nav.tiltakspenger.mottak.søknad.søknadList

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.nav.tiltakspenger.mottak.databind.LocalDateTimeSerializer
import no.nav.tiltakspenger.mottak.joark.models.JoarkSoknad
import no.nav.tiltakspenger.mottak.søknad.ArenaTiltak
import no.nav.tiltakspenger.mottak.søknad.BrukerregistrertTiltak
import java.time.LocalDateTime

@Serializable
data class Søknad(
    val id: String,
    val fornavn: String?,
    val etternavn: String?,
    val ident: String,
    val deltarKvp: Boolean,
    val deltarIntroduksjonsprogrammet: Boolean?,
    val oppholdInstitusjon: Boolean?,
    val typeInstitusjon: String?,
    @Serializable(with = LocalDateTimeSerializer::class)
    val opprettet: LocalDateTime?,
    val barnetillegg: List<Barnetillegg>,
    val arenaTiltak: ArenaTiltak?,
    val brukerregistrertTiltak: BrukerregistrertTiltak?,
    val trygdOgPensjon: List<TrygdOgPensjon>? = null,
) {

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }

        fun fromJson(json: String): Søknad {
            val joarkSoknad = this.json.decodeFromString<JoarkSoknad>(json)
            val personalia = joarkSoknad.fakta.firstOrNull { it.key == "personalia" }
            val fnr = personalia?.properties?.fnr
            requireNotNull(fnr) { "No fnr, cannot handle soknad with id ${joarkSoknad.soknadId}" }
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
            val arenaTiltak = ArenaTiltak.fromJoarkSoknad(joarkSoknad)
            val brukerregistrertTiltak = BrukerregistrertTiltak.fromJoarkSoknad(joarkSoknad)
            val barneTillegg = joarkSoknad.fakta
                .filter { it.key == "barn" }
                .filter { it.properties?.sokerbarnetillegg ?: false }
                .map {
                    Barnetillegg(
                        ident = it.properties?.fnr,
                        fødselsdato = it.properties?.fodselsdato,
                        alder = it.properties?.alder!!.toInt(),
                        land = it.properties.land!!
                    )
                }
            val trygdOgPensjon = joarkSoknad.fakta
                .filter { it.key == "trygdogpensjon.utbetalere" && it.properties?.utbetaler != null }
                .map {
                    TrygdOgPensjon(
                        utbetaler = it.properties?.utbetaler!!,
                        prosent = it.properties.prosent,
                        fom = it.properties.fom!!,
                        tom = it.properties.tom
                    )
                }.ifEmpty { null }

            return Søknad(
                id = joarkSoknad.soknadId.toString(),
                fornavn = personalia.properties.fornavn,
                etternavn = personalia.properties.etternavn,
                ident = fnr,
                deltarKvp = deltarKvp,
                deltarIntroduksjonsprogrammet = deltarIntroduksjonsprogrammet,
                oppholdInstitusjon = oppholdInstitusjon,
                typeInstitusjon = typeInstitusjon,
                opprettet = joarkSoknad.opprettetDato,
                barnetillegg = barneTillegg,
                arenaTiltak = arenaTiltak,
                brukerregistrertTiltak = brukerregistrertTiltak,
                trygdOgPensjon = trygdOgPensjon
            )
        }
    }
}
