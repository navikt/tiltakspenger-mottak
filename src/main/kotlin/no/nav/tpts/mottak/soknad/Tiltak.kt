package no.nav.tpts.mottak.soknad

import kotlinx.serialization.Serializable
import no.nav.tpts.mottak.databind.LocalDateSerializer
import no.nav.tpts.mottak.joark.models.JoarkSoknad
import java.time.LocalDate

@Serializable
data class Tiltak(
    val id: String? = null,
    val arenaId: String? = null,
    val arrangoer: String? = null,
    val harSluttdatoFraArena: Boolean? = null,
    val navn: String? = null,
    @Serializable(with = LocalDateSerializer::class) val opprinneligSluttdato: LocalDate? = null,
    @Serializable(with = LocalDateSerializer::class) val opprinneligStartdato: LocalDate? = null
) {
    companion object {
        fun fromJoarkSoknad(joarkSoknad: JoarkSoknad): Tiltak {
            val valgtTiltakId = joarkSoknad.fakta.firstOrNull { it.key == "tiltaksliste.valgtTiltak" }?.value
            val arenaTiltak = joarkSoknad.fakta
                .firstOrNull { it.key == "tiltaksliste.tiltakFraArena" && it.faktumId.toString() == valgtTiltakId }
            return Tiltak(
                id = arenaTiltak?.properties?.arenaId,
                navn = arenaTiltak?.properties?.navn,
                arrangoer = arenaTiltak?.properties?.arrangoer
            )
        }
    }
}
