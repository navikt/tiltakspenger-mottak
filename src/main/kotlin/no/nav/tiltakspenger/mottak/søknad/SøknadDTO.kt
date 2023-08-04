package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import no.nav.tiltakspenger.mottak.saf.VedleggMetadata
import no.nav.tiltakspenger.mottak.serder.LocalDateTimeSerializer
import no.nav.tiltakspenger.mottak.søknad.SpmSvarDTO.FeilaktigBesvart
import no.nav.tiltakspenger.mottak.søknad.SpmSvarDTO.IkkeBesvart
import no.nav.tiltakspenger.mottak.søknad.SpmSvarDTO.IkkeMedISøknaden
import no.nav.tiltakspenger.mottak.søknad.SpmSvarDTO.Ja
import no.nav.tiltakspenger.mottak.søknad.SpmSvarDTO.Nei
import no.nav.tiltakspenger.mottak.søknad.models.Alderspensjon
import no.nav.tiltakspenger.mottak.søknad.models.Gjenlevendepensjon
import no.nav.tiltakspenger.mottak.søknad.models.JoarkSøknad
import no.nav.tiltakspenger.mottak.søknad.models.Jobbsjansen
import no.nav.tiltakspenger.mottak.søknad.models.Pensjonsordning
import no.nav.tiltakspenger.mottak.søknad.models.Periode
import no.nav.tiltakspenger.mottak.søknad.models.Supplerendestønadflyktninger
import no.nav.tiltakspenger.mottak.søknad.models.Supplerendestønadover67
import no.nav.tiltakspenger.mottak.søknad.models.SøknadFraJoarkV2DTO
import java.lang.Error
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class SøknadDTO(
    val søknadId: String,
    val dokInfo: DokumentInfoDTO,
    val personopplysninger: PersonopplysningerDTO,
    val arenaTiltak: ArenaTiltakDTO?,
    val brukerTiltak: BrukerTiltakDTO?,
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
    val lønnetArbeid: JaNeiSpmDTO,
    @Serializable(with = LocalDateTimeSerializer::class)
    val opprettet: LocalDateTime,
) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }

//        private val objectMapper = jacksonObjectMapper()
//            .registerModule(JavaTimeModule())
//            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

        fun hentVersjon(json: String): String? {
            return Companion.json.parseToJsonElement(json).jsonObject["versjon"]?.toString()?.replace(""""""", "")
        }

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

            val spørsmålsbesvarelserOrginal = soknadOrginal.spørsmålsbesvarelserDTO
            val soknad = if (!spørsmålsbesvarelserOrginal.mottarAndreUtbetalinger) {
                soknadOrginal.copy(
                    spørsmålsbesvarelserDTO = spørsmålsbesvarelserOrginal.copy(
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
                dokInfo = dokInfo,
                personopplysninger = PersonopplysningerDTO(
                    ident = soknad.personopplysninger.ident,
                    fornavn = soknad.personopplysninger.fornavn,
                    etternavn = soknad.personopplysninger.etternavn,
                ),
                arenaTiltak = ArenaTiltakDTO(
                    arenaId = spørsmålsbesvarelserOrginal.tiltak.aktivitetId,
                    arrangoernavn = spørsmålsbesvarelserOrginal.tiltak.arrangør,
                    tiltakskode = spørsmålsbesvarelserOrginal.tiltak.type,
                    opprinneligSluttdato = spørsmålsbesvarelserOrginal.tiltak.arenaRegistrertPeriode?.til,
                    opprinneligStartdato = spørsmålsbesvarelserOrginal.tiltak.arenaRegistrertPeriode?.fra,
                    sluttdato = spørsmålsbesvarelserOrginal.tiltak.periode.til,
                    startdato = spørsmålsbesvarelserOrginal.tiltak.periode.fra,
                ),
                brukerTiltak = null,
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
                    svar = if (spørsmålsbesvarelserOrginal.etterlønn.mottar == null) {
                        if (!spørsmålsbesvarelserOrginal.mottarAndreUtbetalinger) Nei else IkkeBesvart
                    } else {
                        if (spørsmålsbesvarelserOrginal.etterlønn.mottar == true) Ja else Nei
                    },
                ),
                gjenlevendepensjon = mapPeriodeSpm(
                    mottar = spørsmålsbesvarelserOrginal.gjenlevendepensjon.mottar,
                    periode = spørsmålsbesvarelserOrginal.gjenlevendepensjon.periode,
                ),
                alderspensjon = mapFraOgMedSpm(
                    mottar = spørsmålsbesvarelserOrginal.alderspensjon.mottar,
                    fraDato = spørsmålsbesvarelserOrginal.alderspensjon.fraDato,
                ),
                sykepenger = mapPeriodeSpm(
                    mottar = spørsmålsbesvarelserOrginal.sykepenger.mottar,
                    periode = spørsmålsbesvarelserOrginal.sykepenger.periode,
                ),
                supplerendeStønadAlder = mapPeriodeSpm(
                    mottar = spørsmålsbesvarelserOrginal.supplerendestønadover67.mottar,
                    periode = spørsmålsbesvarelserOrginal.supplerendestønadover67.periode,
                ),
                supplerendeStønadFlyktning = mapPeriodeSpm(
                    mottar = spørsmålsbesvarelserOrginal.supplerendestønadflyktninger.mottar,
                    periode = spørsmålsbesvarelserOrginal.supplerendestønadflyktninger.periode,
                ),
                jobbsjansen = mapPeriodeSpm(
                    mottar = spørsmålsbesvarelserOrginal.jobbsjansen.mottar,
                    periode = spørsmålsbesvarelserOrginal.jobbsjansen.periode,
                ),
                trygdOgPensjon = mapPeriodeSpm(
                    mottar = spørsmålsbesvarelserOrginal.pensjonsordning.mottar,
                    periode = spørsmålsbesvarelserOrginal.pensjonsordning.periode,
                ),
                lønnetArbeid = JaNeiSpmDTO(
                    svar = if (spørsmålsbesvarelserOrginal.lønnetArbeid.erILønnetArbeid == null) {
                        IkkeBesvart
                    } else {
                        if (spørsmålsbesvarelserOrginal.lønnetArbeid.erILønnetArbeid) Ja else Nei
                    },
                ),
                opprettet = soknad.innsendingTidspunkt,
            )
        }

        private fun mapPeriodeSpm(mottar: Boolean?, periode: Periode?) =
            if (mottar == null) {
                PeriodeSpmDTO(
                    svar = IkkeBesvart,
                    fom = null,
                    tom = null,
                )
            } else {
                if (mottar == true) {
                    if (periode == null) {
                        PeriodeSpmDTO(
                            svar = FeilaktigBesvart,
                            fom = null,
                            tom = null,
                        )
                    } else {
                        PeriodeSpmDTO(
                            svar = Ja,
                            fom = periode.fra,
                            tom = periode.til,
                        )
                    }
                } else {
                    PeriodeSpmDTO(
                        svar = Nei,
                        fom = null,
                        tom = null,
                    )
                }
            }

        private fun mapFraOgMedSpm(mottar: Boolean?, fraDato: LocalDate?) =
            if (mottar == null) {
                FraOgMedDatoSpmDTO(
                    svar = IkkeBesvart,
                    fom = null,
                )
            } else {
                if (mottar == true) {
                    if (fraDato == null) {
                        FraOgMedDatoSpmDTO(
                            svar = FeilaktigBesvart,
                            fom = null,
                        )
                    } else {
                        FraOgMedDatoSpmDTO(
                            svar = Ja,
                            fom = fraDato,
                        )
                    }
                } else {
                    FraOgMedDatoSpmDTO(
                        svar = Nei,
                        fom = null,
                    )
                }
            }

        fun fromGammelSøknad(
            json: String,
            dokInfo: DokumentInfoDTO,
            vedleggMetadata: List<VedleggMetadata> = emptyList(),
        ): SøknadDTO {
            val joarkSøknad = Companion.json.decodeFromString<JoarkSøknad>(json)

            val arenaTiltak = ArenaTiltakDTO.fromV1Soknad(joarkSøknad)
            val brukerTiltak = BrukerTiltakDTO.fromV1Soknad(joarkSøknad)
            val tiltakPeriode = hentTiltaksperiode(arenaTiltak, brukerTiltak)
            val vedlegg = vedleggMetadata.map {
                DokumentInfoDTO(
                    journalpostId = it.journalpostId,
                    dokumentInfoId = it.dokumentInfoId,
                    filnavn = it.filnavn,
                )
            }

            return SøknadDTO(
                søknadId = joarkSøknad.soknadId.toString(),
                dokInfo = dokInfo,
                personopplysninger = hentPersonopplysninger(joarkSøknad),
                arenaTiltak = arenaTiltak,
                brukerTiltak = brukerTiltak,
                barnetilleggPdl = hentBarnetilleggPdl(joarkSøknad),
                barnetilleggManuelle = hentBarnetilleggManuelle(joarkSøknad),
                vedlegg = vedlegg,
                kvp = hentKvp(joarkSøknad, tiltakPeriode),
                intro = hentIntro(joarkSøknad, tiltakPeriode),
                institusjon = hentInstitusjon(joarkSøknad, tiltakPeriode),
                etterlønn = hentEtterlønn(joarkSøknad),
                gjenlevendepensjon = PeriodeSpmDTO(
                    svar = IkkeMedISøknaden,
                    fom = null,
                    tom = null,
                ),
                alderspensjon = FraOgMedDatoSpmDTO(
                    svar = IkkeMedISøknaden,
                    fom = null,
                ),
                sykepenger = PeriodeSpmDTO(
                    svar = IkkeMedISøknaden,
                    fom = null,
                    tom = null,
                ),
                supplerendeStønadAlder = PeriodeSpmDTO(
                    svar = IkkeMedISøknaden,
                    fom = null,
                    tom = null,
                ),
                supplerendeStønadFlyktning = PeriodeSpmDTO(
                    svar = IkkeMedISøknaden,
                    fom = null,
                    tom = null,
                ),
                jobbsjansen = PeriodeSpmDTO(
                    svar = IkkeMedISøknaden,
                    fom = null,
                    tom = null,
                ),
                trygdOgPensjon = PeriodeSpmDTO(
                    svar = IkkeMedISøknaden,
                    fom = null,
                    tom = null,
                ),
                lønnetArbeid = JaNeiSpmDTO(
                    svar = IkkeMedISøknaden,
                ),
                opprettet = joarkSøknad.opprettetDato,
            )
        }

        private fun hentPersonopplysninger(joarkSøknad: JoarkSøknad): PersonopplysningerDTO {
            val personalia = joarkSøknad.fakta.firstOrNull { it.key == "personalia" }
            val fnr = personalia?.properties?.fnr
            requireNotNull(fnr) { "Mangler fnr, kan ikke behandle søknad med id ${joarkSøknad.soknadId}" }
            return PersonopplysningerDTO(
                ident = fnr,
                fornavn = personalia.properties.fornavn ?: "Mangler Fornavn",
                etternavn = personalia.properties.etternavn ?: "Mangler Etternavn",
            )
        }

        private fun hentTiltaksperiode(arenaTiltak: ArenaTiltakDTO?, brukerTiltak: BrukerTiltakDTO?): Periode {
            return if (arenaTiltak != null) {
                Periode(
                    arenaTiltak.startdato,
                    arenaTiltak.startdato,
                )
            } else {
                if (brukerTiltak != null) {
                    Periode(
                        brukerTiltak.fom,
                        brukerTiltak.tom,
                    )
                } else {
                    Periode(
                        LocalDate.MIN,
                        LocalDate.MAX,
                    )
                }
            }
        }

        private fun hentKvp(joarkSøknad: JoarkSøknad, tiltakPeriode: Periode): PeriodeSpmDTO {
            val deltarKvp =
                joarkSøknad.fakta.firstOrNull { it.key == "informasjonsside.kvalifiseringsprogram" }?.value == "ja"
            return PeriodeSpmDTO(
                svar = if (deltarKvp) Ja else Nei,
                fom = tiltakPeriode.fra,
                tom = tiltakPeriode.til,
            )
        }

        private fun hentIntro(joarkSøknad: JoarkSøknad, tiltakPeriode: Periode): PeriodeSpmDTO {
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
            val introFom = introduksjonsprogrammetProperties?.fom ?: tiltakPeriode.fra
            val introTom = introduksjonsprogrammetProperties?.tom ?: tiltakPeriode.til

            return if (deltarIntroduksjonsprogrammet == null) {
                PeriodeSpmDTO(
                    svar = IkkeBesvart,
                    fom = null,
                    tom = null,
                )
            } else {
                if (deltarIntroduksjonsprogrammet) {
                    PeriodeSpmDTO(
                        svar = Ja,
                        fom = introFom,
                        tom = introTom,
                    )
                } else {
                    PeriodeSpmDTO(
                        svar = Nei,
                        fom = null,
                        tom = null,
                    )
                }
            }
        }

        private fun hentInstitusjon(joarkSøknad: JoarkSøknad, tiltakPeriode: Periode): PeriodeSpmDTO {
            val oppholdInstitusjon =
                joarkSøknad.fakta.first { it.key == "informasjonsside.institusjon" }.value == "ja"
            val typeInstitusjon = joarkSøknad.fakta
                .firstOrNull { it.key == "informasjonsside.institusjon.ja.hvaslags" }?.value

            return if (oppholdInstitusjon) {
                if (typeInstitusjon.equals("Barneverninstitusjon", ignoreCase = true) ||
                    typeInstitusjon.equals("overgangsbolig", ignoreCase = true)
                ) {
                    PeriodeSpmDTO(
                        svar = Nei,
                        fom = null,
                        tom = null,
                    )
                } else {
                    PeriodeSpmDTO(
                        svar = Ja,
                        fom = tiltakPeriode.fra,
                        tom = tiltakPeriode.til,
                    )
                }
            } else {
                PeriodeSpmDTO(
                    svar = Nei,
                    fom = null,
                    tom = null,
                )
            }
        }

        private fun hentBarnetilleggPdl(joarkSøknad: JoarkSøknad): List<BarnetilleggDTO> {
            return joarkSøknad.fakta
                .filter { it.key == "barn" }
                .filter { !it.properties.fnr.isNullOrEmpty() }
                .map {
                    BarnetilleggDTO(
                        fødselsdato = it.properties.fodselsdato ?: toFødselsdato(it.properties.fnr!!),
                        fornavn = it.properties.fornavn,
                        mellomnavn = it.properties.mellomnavn,
                        etternavn = it.properties.etternavn,
                        oppholderSegIEØS = if (it.properties.land == null) {
                            JaNeiSpmDTO(IkkeBesvart)
                        } else {
                            if (it.properties.land.erEøs()) JaNeiSpmDTO(Ja) else JaNeiSpmDTO(Nei)
                        },
                    )
                }
        }

        private fun hentBarnetilleggManuelle(joarkSøknad: JoarkSøknad): List<BarnetilleggDTO> {
            return joarkSøknad.fakta
                .filter { it.key == "barn" }
                .filter { it.properties.fnr.isNullOrEmpty() }
                .map {
                    BarnetilleggDTO(
                        fødselsdato = it.properties.fodselsdato,
                        fornavn = it.properties.fornavn,
                        mellomnavn = it.properties.mellomnavn,
                        etternavn = it.properties.etternavn,
                        oppholderSegIEØS = if (it.properties.land == null) {
                            JaNeiSpmDTO(IkkeBesvart)
                        } else {
                            if (it.properties.land.erEøs()) JaNeiSpmDTO(Ja) else JaNeiSpmDTO(Nei)
                        },
                    )
                }
        }

        private fun hentEtterlønn(joarkSøknad: JoarkSøknad): JaNeiSpmDTO {
            val trygdogpensjonListe = joarkSøknad.fakta
                .filter { it.key == "trygdogpensjon.utbetalere" && it.properties.utbetaler != null }

            return if (trygdogpensjonListe.isEmpty()) JaNeiSpmDTO(Nei) else JaNeiSpmDTO(Ja)
        }

        private fun toFødselsdato(ident: String): LocalDate {
            val mm = ident.subSequence(2, 3).toString().toInt()
            return if (mm > 2) {
                val fnr = ident.subSequence(0, 2).toString() + (mm - 8).toString() + ident.subSequence(3, 6).toString()
                LocalDate.parse(fnr, DateTimeFormatter.ofPattern("ddMMuu"))
            } else {
                LocalDate.parse(ident.subSequence(0, 6), DateTimeFormatter.ofPattern("ddMMuu"))
            }
        }
    }
}
