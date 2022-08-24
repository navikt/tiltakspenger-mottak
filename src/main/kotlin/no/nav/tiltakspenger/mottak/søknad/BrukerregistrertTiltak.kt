package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.mottak.databind.LocalDateSerializer
import java.time.LocalDate

@Serializable
data class BrukerregistrertTiltak(
    val tiltakstype: String,
    @Serializable(with = LocalDateSerializer::class) val tom: LocalDate?,
    val postnummer: String? = null,
    @Serializable(with = LocalDateSerializer::class) val fom: LocalDate?,
    val adresse: String? = null,
    val arrangoernavn: String,
    val antallDager: Int
)
