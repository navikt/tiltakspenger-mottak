package no.nav.tiltakspenger.mottak.søknad.models

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.mottak.serder.LocalDateTimeWithoutZoneSerializer
import no.nav.tiltakspenger.mottak.serder.StrictLocalDateSerializer
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
data class Periode(
    @Serializable(with = StrictLocalDateSerializer::class)
    val fra: LocalDate,
    @Serializable(with = StrictLocalDateSerializer::class)
    val til: LocalDate,
)

@Serializable
data class ManueltRegistrertBarn(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    @Serializable(with = StrictLocalDateSerializer::class)
    val fødselsdato: LocalDate,
    val bostedsland: String,
    val oppholderSegUtenforEøs: Boolean,
)

@Serializable
data class RegistrertBarn(
    val ident: String,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    @Serializable(with = StrictLocalDateSerializer::class)
    val fødselsdato: LocalDate,
    val bostedsland: String,
    val oppholderSegUtenforEøs: Boolean,
)

@Serializable
data class Kvalifiseringsprogram(
    val deltar: Boolean,
    val periode: Periode?,
)

@Serializable
data class Introduksjonsprogram(
    val deltar: Boolean,
    val periode: Periode?,
)

@Serializable
data class Institusjonsopphold(
    val borPåInstitusjon: Boolean,
    val periode: Periode?,
)

@Serializable
data class Tiltak(
    val aktivitetId: String,
    val periode: Periode?,
    val søkerHeleTiltaksperioden: Boolean,
)

@Serializable
data class Barnetillegg(
    val manueltRegistrerteBarnSøktBarnetilleggFor: List<ManueltRegistrertBarn>,
    val registrerteBarnSøktBarnetilleggFor: List<RegistrertBarn>,
)

@Serializable
data class Pensjonsordning(
    val mottarEllerSøktPensjonsordning: Boolean,
    val utbetaler: String?,
    val periode: Periode?,
)

@Serializable
data class Etterlønn(
    val mottarEllerSøktEtterlønn: Boolean,
    val utbetaler: String?,
    val periode: Periode?,
)

@Serializable
data class Personopplysninger(
    val ident: String,
    val fornavn: String,
    val etternavn: String,
)

@Serializable
data class SøknadFraJoarkV2DTO(
    val id: String,
    val kvalifiseringsprogram: Kvalifiseringsprogram,
    val introduksjonsprogram: Introduksjonsprogram,
    val institusjonsopphold: Institusjonsopphold,
    val tiltak: Tiltak,
    val barnetillegg: Barnetillegg,
    val pensjonsordning: Pensjonsordning,
    val etterlønn: Etterlønn,
    val personopplysninger: Personopplysninger,
    @Serializable(with = LocalDateTimeWithoutZoneSerializer::class)
    val innsendingTidspunkt: LocalDateTime,
)
