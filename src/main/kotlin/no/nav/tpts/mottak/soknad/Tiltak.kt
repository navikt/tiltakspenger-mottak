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
            val valgtArenaTiltak = joarkSoknad.fakta
                .firstOrNull { it.key == "tiltaksliste.tiltakFraArena" && it.faktumId.toString() == valgtTiltakId }
            return Tiltak(
                id = valgtArenaTiltak?.properties?.arenaId,
                navn = valgtArenaTiltak?.properties?.navn,
                arrangoer = valgtArenaTiltak?.properties?.arrangoer,
                opprinneligStartdato = valgtArenaTiltak?.properties?.opprinneligstartdato,
                opprinneligSluttdato = valgtArenaTiltak?.properties?.opprinneligsluttdato
            )
        }
    }
}
