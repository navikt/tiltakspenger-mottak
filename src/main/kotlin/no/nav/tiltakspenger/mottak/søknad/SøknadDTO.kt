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

            val spørsmålsbesvarelserOrginal = soknadOrginal.spørsmålsbesvarelser
            val soknad = if (!spørsmålsbesvarelserOrginal.mottarAndreUtbetalinger) {
                soknadOrginal.copy(
                    spørsmålsbesvarelser = spørsmålsbesvarelserOrginal.copy(
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
                    aktivitetId = spørsmålsbesvarelserOrginal.tiltak.aktivitetId,
                    periode = spørsmålsbesvarelserOrginal.tiltak.periode,
                    arenaRegistrertPeriode = spørsmålsbesvarelserOrginal.tiltak.arenaRegistrertPeriode,
                    arrangør = spørsmålsbesvarelserOrginal.tiltak.arrangør,
                    type = spørsmålsbesvarelserOrginal.tiltak.type,
                    typeNavn = spørsmålsbesvarelserOrginal.tiltak.typeNavn,
                ),
                barnetilleggPdl = spørsmålsbesvarelserOrginal.barnetillegg.registrerteBarnSøktBarnetilleggFor.map {
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
                barnetilleggManuelle = spørsmålsbesvarelserOrginal.barnetillegg.manueltRegistrerteBarnSøktBarnetilleggFor.map {
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
                    mottar = spørsmålsbesvarelserOrginal.kvalifiseringsprogram.deltar,
                    periode = spørsmålsbesvarelserOrginal.kvalifiseringsprogram.periode,
                ),
                intro = mapPeriodeSpm(
                    mottar = spørsmålsbesvarelserOrginal.introduksjonsprogram.deltar,
                    periode = spørsmålsbesvarelserOrginal.introduksjonsprogram.periode,
                ),
                institusjon = mapPeriodeSpm(
                    mottar = spørsmålsbesvarelserOrginal.institusjonsopphold.borPåInstitusjon,
                    periode = spørsmålsbesvarelserOrginal.institusjonsopphold.periode,
                ),
                etterlønn = JaNeiSpmDTO(
                    svar = if (spørsmålsbesvarelserOrginal.etterlønn.mottar) Ja else Nei,
                ),
                gjenlevendepensjon = mapPeriodeSpm(
                    mottar = spørsmålsbesvarelserOrginal.gjenlevendepensjon.mottar!!,
                    periode = spørsmålsbesvarelserOrginal.gjenlevendepensjon.periode,
                ),
                alderspensjon = mapFraOgMedSpm(
                    mottar = spørsmålsbesvarelserOrginal.alderspensjon.mottar!!,
                    fraDato = spørsmålsbesvarelserOrginal.alderspensjon.fraDato,
                ),
                sykepenger = mapPeriodeSpm(
                    mottar = spørsmålsbesvarelserOrginal.sykepenger.mottar,
                    periode = spørsmålsbesvarelserOrginal.sykepenger.periode,
                ),
                supplerendeStønadAlder = mapPeriodeSpm(
                    mottar = spørsmålsbesvarelserOrginal.supplerendestønadover67.mottar!!,
                    periode = spørsmålsbesvarelserOrginal.supplerendestønadover67.periode,
                ),
                supplerendeStønadFlyktning = mapPeriodeSpm(
                    mottar = spørsmålsbesvarelserOrginal.supplerendestønadflyktninger.mottar!!,
                    periode = spørsmålsbesvarelserOrginal.supplerendestønadflyktninger.periode,
                ),
                jobbsjansen = mapPeriodeSpm(
                    mottar = spørsmålsbesvarelserOrginal.jobbsjansen.mottar!!,
                    periode = spørsmålsbesvarelserOrginal.jobbsjansen.periode,
                ),
                trygdOgPensjon = mapPeriodeSpm(
                    mottar = spørsmålsbesvarelserOrginal.pensjonsordning.mottar!!,
                    periode = spørsmålsbesvarelserOrginal.pensjonsordning.periode,
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
