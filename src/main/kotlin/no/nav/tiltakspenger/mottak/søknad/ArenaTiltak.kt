package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.nav.tiltakspenger.mottak.databind.LocalDateSerializer
import no.nav.tiltakspenger.mottak.joark.models.JoarkSoknad
import java.time.LocalDate

@Serializable
data class ArenaTiltak(
    val arenaId: String? = null,
    val arrangoer: String? = null,
    val harSluttdatoFraArena: Boolean? = null,
    val navn: String? = null,
    val erIEndreStatus: Boolean? = null,
    @Serializable(with = LocalDateSerializer::class) val opprinneligSluttdato: LocalDate? = null,
    @Serializable(with = LocalDateSerializer::class) val opprinneligStartdato: LocalDate? = null,
    @Serializable(with = LocalDateSerializer::class) val sluttdato: LocalDate? = null,
    @Serializable(with = LocalDateSerializer::class) val startdato: LocalDate? = null
) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }

        fun fromJson(json: String): ArenaTiltak? {
            val joarkSoknad = this.json.decodeFromString<JoarkSoknad>(json)
            val valgtTiltakId = joarkSoknad.fakta.firstOrNull { it.key == "tiltaksliste.valgtTiltak" }?.value
            val valgtArenaTiltak = joarkSoknad.fakta.firstOrNull {
                it.key == "tiltaksliste.tiltakFraArena" && it.faktumId.toString() == valgtTiltakId
            } ?: return null
            return ArenaTiltak(
                arenaId = valgtArenaTiltak.properties?.arenaId,
                arrangoer = valgtArenaTiltak.properties?.arrangoer,
                harSluttdatoFraArena = valgtArenaTiltak.properties?.harSluttdatoFraArena,
                navn = valgtArenaTiltak.properties?.navn,
                erIEndreStatus = valgtArenaTiltak.properties?.erIEndreStatus,
                opprinneligStartdato = valgtArenaTiltak.properties?.opprinneligstartdato,
                opprinneligSluttdato = valgtArenaTiltak.properties?.opprinneligsluttdato,
                sluttdato = valgtArenaTiltak.properties?.sluttdato,
                startdato = valgtArenaTiltak.properties?.startdato
            )
        }
    }
}
