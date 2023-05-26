package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.nav.tiltakspenger.mottak.saf.VedleggMetadata
import no.nav.tiltakspenger.mottak.serder.LocalDateTimeSerializer
import no.nav.tiltakspenger.mottak.søknad.SpmSvarDTO.IkkeBesvart
import no.nav.tiltakspenger.mottak.søknad.SpmSvarDTO.IkkeMedISøknaden
import no.nav.tiltakspenger.mottak.søknad.SpmSvarDTO.Ja
import no.nav.tiltakspenger.mottak.søknad.SpmSvarDTO.Nei
import no.nav.tiltakspenger.mottak.søknad.models.JoarkSøknad
import no.nav.tiltakspenger.mottak.søknad.models.Periode
import no.nav.tiltakspenger.mottak.søknad.models.SøknadFraJoarkV2DTO
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class SøknadDTO(
    val versjon: String,
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
    val gjenlevendepensjon: FraOgMedDatoSpmDTO,
    val alderspensjon: FraOgMedDatoSpmDTO,
    val sykepenger: PeriodeSpmDTO,
    val supplerendeStønadAlder: PeriodeSpmDTO,
    val supplerendeStønadFlyktning: PeriodeSpmDTO,
    val jobbsjansen: PeriodeSpmDTO,
    val trygdOgPensjon: FraOgMedDatoSpmDTO,
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

        fun fromNySøknad(
            json: String,
            dokInfo: DokumentInfoDTO,
            vedleggMetadata: List<VedleggMetadata> = emptyList(),
        ): SøknadDTO {
            val soknad = Companion.json.decodeFromString<SøknadFraJoarkV2DTO>(json)
            val vedlegg = vedleggMetadata.map {
                DokumentInfoDTO(
                    journalpostId = it.journalpostId,
                    dokumentInfoId = it.dokumentInfoId,
                    filnavn = it.filnavn,
                )
            }

            return SøknadDTO(
                versjon = "2",
                søknadId = soknad.id.toString(),
                dokInfo = dokInfo,
                personopplysninger = PersonopplysningerDTO(
                    ident = soknad.personopplysninger.ident,
                    fornavn = soknad.personopplysninger.fornavn,
                    etternavn = soknad.personopplysninger.etternavn,
                ),
                arenaTiltak = ArenaTiltakDTO(
                    arenaId = soknad.tiltak.aktivitetId,
                    arrangoernavn = "---",
                    tiltakskode = "",
                    opprinneligSluttdato = soknad.tiltak.periode?.til,
                    opprinneligStartdato = soknad.tiltak.periode!!.fra,
                    sluttdato = soknad.tiltak.periode.til,
                    startdato = soknad.tiltak.periode.fra,
                ),
                brukerTiltak = null,
                barnetilleggPdl = soknad.barnetillegg.registrerteBarnSøktBarnetilleggFor.map {
                    BarnetilleggDTO(
                        fødselsdato = it.fødselsdato,
                        fornavn = it.fornavn,
                        mellomnavn = it.mellomnavn,
                        etternavn = it.etternavn,
                        oppholderSegIEØS = JaNeiSpmDTO(
                            svar = if (it.oppholderSegUtenforEøs) Ja else Nei,
                        ),
                    )
                },
                barnetilleggManuelle = emptyList(),
                vedlegg = vedlegg,
                kvp = if (soknad.kvalifiseringsprogram.deltar) {
                    PeriodeSpmDTO(
                        svar = Ja,
                        fom = soknad.kvalifiseringsprogram.periode!!.fra,
                        tom = soknad.kvalifiseringsprogram.periode.til,
                    )
                } else {
                    PeriodeSpmDTO(
                        svar = Nei,
                        fom = null,
                        tom = null,
                    )
                },
                intro = if (soknad.introduksjonsprogram.deltar) {
                    PeriodeSpmDTO(
                        svar = Ja,
                        fom = soknad.introduksjonsprogram.periode!!.fra,
                        tom = soknad.introduksjonsprogram.periode.til,
                    )
                } else {
                    PeriodeSpmDTO(
                        svar = Nei,
                        fom = null,
                        tom = null,
                    )
                },
                institusjon = if (soknad.institusjonsopphold.borPåInstitusjon) {
                    PeriodeSpmDTO(
                        svar = Ja,
                        fom = soknad.institusjonsopphold.periode!!.fra,
                        tom = soknad.institusjonsopphold.periode.til,
                    )
                } else {
                    PeriodeSpmDTO(
                        svar = Nei,
                        fom = null,
                        tom = null,
                    )
                },
                etterlønn = JaNeiSpmDTO(
                    svar = if (soknad.etterlønn.mottarEllerSøktEtterlønn) Ja else Nei,
                ),
                gjenlevendepensjon = FraOgMedDatoSpmDTO(
                    svar = Nei,
                    fom = null,
                ),
                alderspensjon = FraOgMedDatoSpmDTO(
                    svar = Nei,
                    fom = null,
                ),
                sykepenger = PeriodeSpmDTO(
                    svar = Nei,
                    fom = null,
                    tom = null,
                ),
                supplerendeStønadAlder = PeriodeSpmDTO(
                    svar = Nei,
                    fom = null,
                    tom = null,
                ),
                supplerendeStønadFlyktning = PeriodeSpmDTO(
                    svar = Nei,
                    fom = null,
                    tom = null,
                ),
                jobbsjansen = PeriodeSpmDTO(
                    svar = Nei,
                    fom = null,
                    tom = null,
                ),
                trygdOgPensjon = FraOgMedDatoSpmDTO(
                    svar = Nei,
                    fom = null,
                ),
                opprettet = LocalDateTime.now(),
            )
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
                versjon = "1",
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
                gjenlevendepensjon = FraOgMedDatoSpmDTO(
                    svar = IkkeMedISøknaden,
                    fom = null,
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
                trygdOgPensjon = FraOgMedDatoSpmDTO(
                    svar = IkkeMedISøknaden,
                    fom = null,
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

        private fun toFødselsdato(ident: String): LocalDate =
            LocalDate.parse(ident.subSequence(0, 6), DateTimeFormatter.ofPattern("ddMMuu"))
    }
}
