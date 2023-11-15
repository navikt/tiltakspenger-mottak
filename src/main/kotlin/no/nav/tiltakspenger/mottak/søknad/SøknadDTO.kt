package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import no.nav.tiltakspenger.mottak.saf.VedleggMetadata
import no.nav.tiltakspenger.mottak.serder.LocalDateTimeSerializer
import no.nav.tiltakspenger.mottak.søknad.SpmSvarDTO.Ja
import no.nav.tiltakspenger.mottak.søknad.SpmSvarDTO.Nei
import no.nav.tiltakspenger.mottak.søknad.models.Alderspensjon
import no.nav.tiltakspenger.mottak.søknad.models.Gjenlevendepensjon
import no.nav.tiltakspenger.mottak.søknad.models.Jobbsjansen
import no.nav.tiltakspenger.mottak.søknad.models.Pensjonsordning
import no.nav.tiltakspenger.mottak.søknad.models.Periode
import no.nav.tiltakspenger.mottak.søknad.models.Supplerendestønadflyktninger
import no.nav.tiltakspenger.mottak.søknad.models.Supplerendestønadover67
import no.nav.tiltakspenger.mottak.søknad.models.SøknadFraJoarkV2DTO
import java.lang.Error
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
data class SøknadDTO(
    val versjon: String,
    val søknadId: String,
    val dokInfo: DokumentInfoDTO,
    val personopplysninger: PersonopplysningerDTO,
    val tiltak: TiltakDTO,
    val barnetilleggPdl: List<BarnetilleggDTO>,
    val barnetilleggManuelle: List<BarnetilleggDTO>,
    val vedlegg: List<DokumentInfoDTO>,
    val kvp: PeriodeSpmDTO,
    val intro: PeriodeSpmDTO,
    val institusjon: PeriodeSpmDTO,
    val etterlønn: JaNeiSpmDTO,
    val gjenlevendepensjon: PeriodeSpmDTO,
    val alderspensjon: FraOgMedDatoSpmDTO,
    val sykepenger: PeriodeSpmDTO,
    val supplerendeStønadAlder: PeriodeSpmDTO,
    val supplerendeStønadFlyktning: PeriodeSpmDTO,
    val jobbsjansen: PeriodeSpmDTO,
    val trygdOgPensjon: PeriodeSpmDTO,
    @Serializable(with = LocalDateTimeSerializer::class)
    val opprettet: LocalDateTime,
) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }

//        fun hentVersjon(json: String): String? {
//            return Companion.json.parseToJsonElement(json).jsonObject["versjon"]?.toString()?.replace(""""""", "")
//        }

        fun fromSøknadV4(
            json: String,
            dokInfo: DokumentInfoDTO,
            vedleggMetadata: List<VedleggMetadata> = emptyList(),
        ): SøknadDTO {
            val soknadOrginal = try {
                Companion.json.decodeFromString<SøknadFraJoarkV2DTO>(json)
            } catch (e: Error) {
                throw IllegalStateException("Vi fikk en Søknad med versjonsnummer som ikke matcher inneholdet")
            }

            val vedlegg = vedleggMetadata.map {
                DokumentInfoDTO(
                    journalpostId = it.journalpostId,
                    dokumentInfoId = it.dokumentInfoId,
                    filnavn = it.filnavn,
                )
            }

            val soknad = if (!soknadOrginal.spørsmålsbesvarelser.mottarAndreUtbetalinger) {
                soknadOrginal.copy(
                    spørsmålsbesvarelser = soknadOrginal.spørsmålsbesvarelser.copy(
                        gjenlevendepensjon = Gjenlevendepensjon(false, null),
                        alderspensjon = Alderspensjon(false, null),
                        supplerendestønadflyktninger = Supplerendestønadflyktninger(false, null),
                        supplerendestønadover67 = Supplerendestønadover67(false, null),
                        jobbsjansen = Jobbsjansen(false, null),
                        pensjonsordning = Pensjonsordning(false, null),
                    ),
                )
            } else {
                soknadOrginal
            }

            return SøknadDTO(
                søknadId = soknad.id,
                versjon = soknad.versjon,
                dokInfo = dokInfo,
                personopplysninger = PersonopplysningerDTO(
                    ident = soknad.personopplysninger.ident,
                    fornavn = soknad.personopplysninger.fornavn,
                    etternavn = soknad.personopplysninger.etternavn,
                ),
                tiltak = TiltakDTO(
                    id = soknad.spørsmålsbesvarelser.tiltak.aktivitetId,
                    deltakelseFom = soknad.spørsmålsbesvarelser.tiltak.periode.fra,
                    deltakelseTom = soknad.spørsmålsbesvarelser.tiltak.periode.til,
                    arrangør = soknad.spørsmålsbesvarelser.tiltak.arrangør,
                    typeKode = soknad.spørsmålsbesvarelser.tiltak.type,
                    typeNavn = soknad.spørsmålsbesvarelser.tiltak.typeNavn,
                ),
                barnetilleggPdl = soknad.spørsmålsbesvarelser.barnetillegg.registrerteBarnSøktBarnetilleggFor.map {
                    BarnetilleggDTO(
                        fødselsdato = it.fødselsdato,
                        fornavn = it.fornavn,
                        mellomnavn = it.mellomnavn,
                        etternavn = it.etternavn,
                        oppholderSegIEØS = JaNeiSpmDTO(
                            svar = if (it.oppholdInnenforEøs) Ja else Nei,
                        ),
                    )
                },
                barnetilleggManuelle = soknad.spørsmålsbesvarelser.barnetillegg.manueltRegistrerteBarnSøktBarnetilleggFor.map {
                    BarnetilleggDTO(
                        fødselsdato = it.fødselsdato,
                        fornavn = it.fornavn,
                        mellomnavn = it.mellomnavn,
                        etternavn = it.etternavn,
                        oppholderSegIEØS = JaNeiSpmDTO(
                            svar = if (it.oppholdInnenforEøs) Ja else Nei,
                        ),
                    )
                },
                vedlegg = vedlegg,
                kvp = mapPeriodeSpm(
                    mottar = soknad.spørsmålsbesvarelser.kvalifiseringsprogram.deltar,
                    periode = soknad.spørsmålsbesvarelser.kvalifiseringsprogram.periode,
                ),
                intro = mapPeriodeSpm(
                    mottar = soknad.spørsmålsbesvarelser.introduksjonsprogram.deltar,
                    periode = soknad.spørsmålsbesvarelser.introduksjonsprogram.periode,
                ),
                institusjon = mapPeriodeSpm(
                    mottar = soknad.spørsmålsbesvarelser.institusjonsopphold.borPåInstitusjon,
                    periode = soknad.spørsmålsbesvarelser.institusjonsopphold.periode,
                ),
                etterlønn = JaNeiSpmDTO(
                    svar = if (soknad.spørsmålsbesvarelser.etterlønn.mottar) Ja else Nei,
                ),
                gjenlevendepensjon = mapPeriodeSpm(
                    mottar = soknad.spørsmålsbesvarelser.gjenlevendepensjon.mottar!!,
                    periode = soknad.spørsmålsbesvarelser.gjenlevendepensjon.periode,
                ),
                alderspensjon = mapFraOgMedSpm(
                    mottar = soknad.spørsmålsbesvarelser.alderspensjon.mottar!!,
                    fraDato = soknad.spørsmålsbesvarelser.alderspensjon.fraDato,
                ),
                sykepenger = mapPeriodeSpm(
                    mottar = soknad.spørsmålsbesvarelser.sykepenger.mottar,
                    periode = soknad.spørsmålsbesvarelser.sykepenger.periode,
                ),
                supplerendeStønadAlder = mapPeriodeSpm(
                    mottar = soknad.spørsmålsbesvarelser.supplerendestønadover67.mottar!!,
                    periode = soknad.spørsmålsbesvarelser.supplerendestønadover67.periode,
                ),
                supplerendeStønadFlyktning = mapPeriodeSpm(
                    mottar = soknad.spørsmålsbesvarelser.supplerendestønadflyktninger.mottar!!,
                    periode = soknad.spørsmålsbesvarelser.supplerendestønadflyktninger.periode,
                ),
                jobbsjansen = mapPeriodeSpm(
                    mottar = soknad.spørsmålsbesvarelser.jobbsjansen.mottar!!,
                    periode = soknad.spørsmålsbesvarelser.jobbsjansen.periode,
                ),
                trygdOgPensjon = mapPeriodeSpm(
                    mottar = soknad.spørsmålsbesvarelser.pensjonsordning.mottar!!,
                    periode = soknad.spørsmålsbesvarelser.pensjonsordning.periode,
                ),
                opprettet = soknad.innsendingTidspunkt,
            )
        }

        private fun mapPeriodeSpm(mottar: Boolean, periode: Periode?) =
            if (mottar) {
                PeriodeSpmDTO(
                    svar = Ja,
                    fom = periode?.fra,
                    tom = periode?.til,
                )
            } else {
                PeriodeSpmDTO(
                    svar = Nei,
                    fom = null,
                    tom = null,
                )
            }

        private fun mapFraOgMedSpm(mottar: Boolean, fraDato: LocalDate?) =
            if (mottar) {
                FraOgMedDatoSpmDTO(
                    svar = Ja,
                    fom = fraDato,
                )
            } else {
                FraOgMedDatoSpmDTO(
                    svar = Nei,
                    fom = null,
                )
            }
    }
}
