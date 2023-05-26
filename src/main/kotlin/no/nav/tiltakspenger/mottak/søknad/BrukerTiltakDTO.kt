package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.mottak.serder.StrictLocalDateSerializer
import no.nav.tiltakspenger.mottak.søknad.models.JoarkSøknad
import java.time.LocalDate

@Serializable
data class BrukerTiltakDTO(
    val tiltakskode: String,
    val arrangoernavn: String?,
    val beskrivelse: String?, // kun om det er 'Annet' tiltak
    @Serializable(with = StrictLocalDateSerializer::class) val fom: LocalDate,
    @Serializable(with = StrictLocalDateSerializer::class) val tom: LocalDate,
    val adresse: String? = null,
    val postnummer: String? = null,
    val antallDager: Int,
) {
    companion object {
        fun fromV1Soknad(joarkSøknad: JoarkSøknad): BrukerTiltakDTO? {
            val brukerregistrertTiltakJson =
                joarkSøknad.fakta.firstOrNull { it.key == "tiltaksliste.annetTiltak" } ?: return null
            return BrukerTiltakDTO(
                tiltakskode = brukerregistrertTiltakJson.value!!,
                arrangoernavn = brukerregistrertTiltakJson.properties.arrangoernavn,
                beskrivelse = brukerregistrertTiltakJson.properties.beskrivelse,
                fom = brukerregistrertTiltakJson.properties.fom!!,
                tom = brukerregistrertTiltakJson.properties.tom!!,
                adresse = brukerregistrertTiltakJson.properties.adresse,
                postnummer = brukerregistrertTiltakJson.properties.postnummer,
                antallDager = brukerregistrertTiltakJson.properties.antallDager?.substringBefore(' ')?.toInt() ?: 0,
            )
        }
    }
}
