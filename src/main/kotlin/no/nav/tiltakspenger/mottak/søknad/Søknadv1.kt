package no.nav.tiltakspenger.mottak.søknad

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.nav.tiltakspenger.mottak.saf.VedleggMetadata
import no.nav.tiltakspenger.mottak.serder.LocalDateTimeSerializer
import no.nav.tiltakspenger.mottak.søknad.models.JoarkSøknad
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
data class Søknadv1(
    val søknadId: String,
    val fornavn: String?,
    val etternavn: String?,
    val ident: String,
    val deltarKvp: Boolean,
    val deltarIntroduksjonsprogrammet: Boolean?,
    val introduksjonsprogrammetDetaljer: IntroduksjonsprogrammetDetaljer?,
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

        private val objectMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

        private fun introduksjonsprogrammetDetaljer(fom: LocalDate?, tom: LocalDate?) =
            fom?.let { IntroduksjonsprogrammetDetaljer(fom, tom) }

        fun toSøknad(
            json: String,
            journalpostId: String,
            dokumentInfoId: String,
            filnavn: String,
            vedleggMetadata: List<VedleggMetadata> = emptyList(),
        ): Søknad {
            val dokumentInfo = vedleggMetadata.map {
                DokumentInfo(
                    journalpostId = it.journalpostId,
                    dokumentInfoId = it.dokumentInfoId,
                    filnavn = it.filnavn,
                )
            }
            val søknadv1 = fromJson(json)
            return Søknad(
                ident = søknadv1.ident,
                hoveddokument = DokumentInfo(
                    journalpostId = journalpostId,
                    dokumentInfoId = dokumentInfoId,
                    filnavn = filnavn,
                ),
                versjon = "1",
                søknad = objectMapper.writeValueAsString(søknadv1),
                vedlegg = dokumentInfo,
            )
        }
        fun fromJson(
            json: String,
        ): Søknadv1 {
            val joarkSøknad = Companion.json.decodeFromString<JoarkSøknad>(json)
            val personalia = joarkSøknad.fakta.firstOrNull { it.key == "personalia" }
            val fnr = personalia?.properties?.fnr
            requireNotNull(fnr) { "Mangler fnr, kan ikke behandle søknad med id ${joarkSøknad.soknadId}" }
            val deltarKvp =
                joarkSøknad.fakta.firstOrNull { it.key == "informasjonsside.kvalifiseringsprogram" }?.value == "ja"
            // Faktum "informasjonsside.deltarIIntroprogram" gir strengen "false" når deltaker svarer ja på deltakelse,
            // "true" når deltaker svarer nei på deltakelse og null når søker ikke får spørsmålet, sjekker derfor
            // kommune istedet for å unngå (mer) forvirring
            val introduksjonsprogrammetProperties =
                joarkSøknad.fakta.firstOrNull { it.key == "informasjonsside.deltarIIntroprogram.info" }?.properties
            val deltarIntroduksjonsprogrammet =
                if (joarkSøknad.fakta.first { it.key == "informasjonsside.deltarIIntroprogram" }.value == null) {
                    null
                } else {
                    introduksjonsprogrammetProperties?.kommune?.isNotEmpty() ?: false
                }
            val introduksjonsprogrammetFom = introduksjonsprogrammetProperties?.fom
            val introduksjonsprogrammetTom = introduksjonsprogrammetProperties?.tom
            val oppholdInstitusjon =
                joarkSøknad.fakta.first { it.key == "informasjonsside.institusjon" }.value == "ja"
            val typeInstitusjon = joarkSøknad.fakta
                .firstOrNull { it.key == "informasjonsside.institusjon.ja.hvaslags" }?.value
            val arenaTiltak = ArenaTiltak.fromJoarkSoknad(joarkSøknad)
            val brukerregistrertTiltak = BrukerregistrertTiltak.fromJoarkSoknad(joarkSøknad)
            val barneTillegg = joarkSøknad.fakta
                .filter { it.key == "barn" }
                .map {
                    Barnetillegg(
                        ident = it.properties.fnr,
                        fødselsdato = it.properties.fodselsdato,
                        alder = it.properties.alder!!.toInt(),
                        oppholdsland = it.properties.land!!,
                        fornavn = it.properties.fornavn,
                        mellomnavn = it.properties.mellomnavn,
                        etternavn = it.properties.etternavn,
                        søktBarnetillegg = it.properties.sokerbarnetillegg ?: false,
                    )
                }
            val trygdOgPensjon = joarkSøknad.fakta
                .filter { it.key == "trygdogpensjon.utbetalere" && it.properties.utbetaler != null }
                .map {
                    TrygdOgPensjon(
                        utbetaler = it.properties.utbetaler!!,
                        prosent = it.properties.prosent,
                        fom = it.properties.fom,
                        tom = it.properties.tom,
                    )
                }

            val fritekst = joarkSøknad.fakta.firstOrNull { it.key == "tilleggsopplysninger.fritekst" }?.value

            return Søknadv1(
                søknadId = joarkSøknad.soknadId.toString(),
                fornavn = personalia.properties.fornavn,
                etternavn = personalia.properties.etternavn,
                ident = fnr,
                deltarKvp = deltarKvp,
                deltarIntroduksjonsprogrammet = deltarIntroduksjonsprogrammet,
                introduksjonsprogrammetDetaljer = introduksjonsprogrammetDetaljer(
                    introduksjonsprogrammetFom,
                    introduksjonsprogrammetTom,
                ),
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
