package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.mottak.databind.LocalDateSerializer
import no.nav.tiltakspenger.mottak.joark.models.JoarkSoknad
import java.time.LocalDate

@Serializable
data class BrukerregistrertTiltak(
    val tiltakstype: String,
    val beskrivelse: String?,
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
                // TODO: I soknad_deltar_intro.json er beskrivelse feltet brukt!
                // Ref også https://nav-it.slack.com/archives/C02BBKL9SGM/p1661434113014189?thread_ts=1661327854.713069&cid=C02BBKL9SGM
                // beskrivelse = brukerregistrertTiltakJson.properties?.beskrivelse,
                beskrivelse = null,
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
