package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.mottak.søknad.models.Deltakelsesperiode
import no.nav.tiltakspenger.mottak.søknad.models.Periode

@Serializable
data class TiltakDTO(
    val aktivitetId: String,
    val periode: Periode,
    val arenaRegistrertPeriode: Deltakelsesperiode?,
    val arrangør: String,
    val type: String,
    val typeNavn: String,
)
