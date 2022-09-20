package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.mottak.databind.LocalDateSerializer
import no.nav.tiltakspenger.mottak.joark.models.JoarkSøknad
import java.time.LocalDate

@Serializable
data class BrukerregistrertTiltak(
    val tiltakskode: String,
    val arrangoernavn: String,
    val beskrivelse: String?, // kun om det er 'Annet' tiltak
    @Serializable(with = LocalDateSerializer::class) val fom: LocalDate,
    @Serializable(with = LocalDateSerializer::class) val tom: LocalDate,
    val adresse: String? = null,
    val postnummer: String? = null,
    val antallDager: Int
) {
    companion object {
        fun fromJoarkSoknad(joarkSøknad: JoarkSøknad): BrukerregistrertTiltak? {
            val brukerregistrertTiltakJson =
                joarkSøknad.fakta.firstOrNull { it.key == "tiltaksliste.annetTiltak" } ?: return null
            return BrukerregistrertTiltak(
                tiltakskode = brukerregistrertTiltakJson.value!!,
                arrangoernavn = brukerregistrertTiltakJson.properties.arrangoernavn!!,
                beskrivelse = brukerregistrertTiltakJson.properties.beskrivelse,
                fom = brukerregistrertTiltakJson.properties.fom!!,
                tom = brukerregistrertTiltakJson.properties.tom!!,
                adresse = brukerregistrertTiltakJson.properties.adresse,
                postnummer = brukerregistrertTiltakJson.properties.postnummer,
                antallDager = brukerregistrertTiltakJson.properties.antallDager?.substringBefore(' ')?.toInt()
                    ?: throw IllegalArgumentException("Antall dager ikke valgt")
            )
        }
    }
}
