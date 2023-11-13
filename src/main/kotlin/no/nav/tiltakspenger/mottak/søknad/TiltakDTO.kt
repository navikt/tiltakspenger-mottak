package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.mottak.serder.StrictLocalDateSerializer
import java.time.LocalDate

@Serializable
data class TiltakDTO(
    val id: String,
    @Serializable(with = StrictLocalDateSerializer::class)
    val deltakelseFom: LocalDate,
    @Serializable(with = StrictLocalDateSerializer::class)
    val deltakelseTom: LocalDate,
    val arrangør: String,
    val typeKode: String,
    val typeNavn: String,
)
