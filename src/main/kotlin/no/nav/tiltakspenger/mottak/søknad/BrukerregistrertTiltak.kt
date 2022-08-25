package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.mottak.databind.LocalDateSerializer
import no.nav.tiltakspenger.mottak.joark.models.JoarkSoknad
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
) {
    companion object {
        fun fromJoarkSoknad(joarkSoknad: JoarkSoknad): BrukerregistrertTiltak? {
            val brukerregistrertTiltakJson =
                joarkSoknad.fakta.firstOrNull { it.key == "tiltaksliste.annetTiltak" } ?: return null
            return BrukerregistrertTiltak(
                tiltakstype = brukerregistrertTiltakJson.value!!,
                tom = brukerregistrertTiltakJson.properties?.tom,
                postnummer = brukerregistrertTiltakJson.properties?.postnummer,
                fom = brukerregistrertTiltakJson.properties?.fom,
                adresse = brukerregistrertTiltakJson.properties?.adresse,
                arrangoernavn = brukerregistrertTiltakJson.properties?.arrangoernavn!!,
                antallDager = brukerregistrertTiltakJson.properties.antallDager?.substringBefore(' ')?.toInt()!!
            )
        }
    }
}
