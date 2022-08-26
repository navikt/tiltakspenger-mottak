package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.mottak.databind.LocalDateSerializer
import no.nav.tiltakspenger.mottak.joark.models.JoarkSoknad
import java.time.LocalDate

@Serializable
data class BrukerregistrertTiltak(
    val tiltakstype: String?,
    val arrangoernavn: String?,
    val beskrivelse: String?,
    @Serializable(with = LocalDateSerializer::class) val fom: LocalDate?,
    @Serializable(with = LocalDateSerializer::class) val tom: LocalDate?,
    val adresse: String? = null,
    val postnummer: String? = null,
    val antallDager: Int
) {
    companion object {
        fun fromJoarkSoknad(joarkSoknad: JoarkSoknad): BrukerregistrertTiltak? {
            val brukerregistrertTiltakJson =
                joarkSoknad.fakta.firstOrNull { it.key == "tiltaksliste.annetTiltak" } ?: return null
            return BrukerregistrertTiltak(
                tiltakstype = brukerregistrertTiltakJson.value,
                arrangoernavn = brukerregistrertTiltakJson.properties?.arrangoernavn,
                beskrivelse = brukerregistrertTiltakJson.properties?.beskrivelse,
                fom = brukerregistrertTiltakJson.properties?.fom,
                tom = brukerregistrertTiltakJson.properties?.tom,
                adresse = brukerregistrertTiltakJson.properties?.adresse,
                postnummer = brukerregistrertTiltakJson.properties?.postnummer,
                antallDager = brukerregistrertTiltakJson.properties?.antallDager?.substringBefore(' ')?.toInt()!!
            )
        }
    }
}
