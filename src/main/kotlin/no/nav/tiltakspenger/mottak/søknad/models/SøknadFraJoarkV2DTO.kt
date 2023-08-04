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
data class Deltakelsesperiode(
    @Serializable(with = StrictLocalDateSerializer::class)
    val fra: LocalDate?,
    @Serializable(with = StrictLocalDateSerializer::class)
    val til: LocalDate?,
)

@Serializable
data class ManueltRegistrertBarn(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    @Serializable(with = StrictLocalDateSerializer::class)
    val fødselsdato: LocalDate,
    val oppholdInnenforEøs: Boolean,
)

@Serializable
data class RegistrertBarn(
    val fornavn: String?,
    val mellomnavn: String?,
    val etternavn: String?,
    @Serializable(with = StrictLocalDateSerializer::class)
    val fødselsdato: LocalDate,
    val oppholdInnenforEøs: Boolean,
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
    val periode: Periode,
    val arenaRegistrertPeriode: Deltakelsesperiode?,
    val arrangør: String,
    val type: String,
    val typeNavn: String,
)

@Serializable
data class Barnetillegg(
    val manueltRegistrerteBarnSøktBarnetilleggFor: List<ManueltRegistrertBarn>,
    val registrerteBarnSøktBarnetilleggFor: List<RegistrertBarn>,
)

@Serializable
data class Pensjonsordning(
    val mottar: Boolean?,
    val periode: Periode?,
)

@Serializable
data class LønnetArbeid(
    val erILønnetArbeid: Boolean?,
)

@Serializable
data class Etterlønn(
    val mottar: Boolean?,
)

@Serializable
data class Personopplysninger(
    val ident: String,
    val fornavn: String,
    val etternavn: String,
)

@Serializable
data class Sykepenger(
    val mottar: Boolean?,
    val periode: Periode?,
)

@Serializable
data class Gjenlevendepensjon(
    val mottar: Boolean?,
    val periode: Periode?,
)

@Serializable
data class Alderspensjon(
    val mottar: Boolean?,
    @Serializable(with = StrictLocalDateSerializer::class)
    val fraDato: LocalDate?,
)

@Serializable
data class Supplerendestønadover67(
    val mottar: Boolean?,
    val periode: Periode?,
)

@Serializable
data class Supplerendestønadflyktninger(
    val mottar: Boolean?,
    val periode: Periode?,
)

@Serializable
data class Jobbsjansen(
    val mottar: Boolean?,
    val periode: Periode?,
)

@Serializable
data class SpørsmålsbesvarelserDTO(
    val kvalifiseringsprogram: Kvalifiseringsprogram,
    val introduksjonsprogram: Introduksjonsprogram,
    val institusjonsopphold: Institusjonsopphold,
    val tiltak: Tiltak,
    val barnetillegg: Barnetillegg,
    val mottarAndreUtbetalinger: Boolean,
    val sykepenger: Sykepenger,
    val gjenlevendepensjon: Gjenlevendepensjon,
    val alderspensjon: Alderspensjon,
    val supplerendestønadover67: Supplerendestønadover67,
    val supplerendestønadflyktninger: Supplerendestønadflyktninger,
    val pensjonsordning: Pensjonsordning,
    val etterlønn: Etterlønn,
    val lønnetArbeid: LønnetArbeid,
    val jobbsjansen: Jobbsjansen,
    val harBekreftetAlleOpplysninger: Boolean,
    val harBekreftetÅSvareSåGodtManKan: Boolean,
)

@Serializable
data class SøknadFraJoarkV2DTO(
    val id: String,
    val acr: String,
    val spørsmålsbesvarelserDTO: SpørsmålsbesvarelserDTO,
    val personopplysninger: Personopplysninger,
    val vedleggsnavn: List<String>,
    @Serializable(with = LocalDateTimeWithoutZoneSerializer::class)
    val innsendingTidspunkt: LocalDateTime,
)
