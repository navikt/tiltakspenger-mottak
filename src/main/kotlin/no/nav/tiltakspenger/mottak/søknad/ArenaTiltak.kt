package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.nav.tiltakspenger.mottak.serder.StrictLocalDateSerializer
import no.nav.tiltakspenger.mottak.søknad.models.JoarkSøknad
import java.time.LocalDate

@Serializable
data class ArenaTiltak(
    val arenaId: String,
    val arrangoer: String?, // kan være null, f.eks. "Utvidet oppfølging i NAV"
    val harSluttdatoFraArena: Boolean,
    val tiltakskode: String,
    val erIEndreStatus: Boolean,
    @Serializable(with = StrictLocalDateSerializer::class) val opprinneligSluttdato: LocalDate? = null,
    @Serializable(with = StrictLocalDateSerializer::class) val opprinneligStartdato: LocalDate,
    @Serializable(with = StrictLocalDateSerializer::class) val sluttdato: LocalDate? = null,
    @Serializable(with = StrictLocalDateSerializer::class) val startdato: LocalDate,
) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }

        fun fromJson(json: String): ArenaTiltak? = fromJoarkSoknad(this.json.decodeFromString(json))

        fun fromJoarkSoknad(joarkSøknad: JoarkSøknad): ArenaTiltak? {
            val valgtTiltakId = joarkSøknad.fakta.first { it.key == "tiltaksliste.valgtTiltak" }.value
            val valgtArenaTiltak = joarkSøknad.fakta.firstOrNull {
                it.key == "tiltaksliste.tiltakFraArena" && it.faktumId.toString() == valgtTiltakId
            } ?: return null
            return ArenaTiltak(
                arenaId = valgtArenaTiltak.properties.arenaId!!,
                arrangoer = valgtArenaTiltak.properties.arrangoer,
                harSluttdatoFraArena = valgtArenaTiltak.properties.harSluttdatoFraArena!!,
                tiltakskode = valgtArenaTiltak.value!!,
                erIEndreStatus = valgtArenaTiltak.properties.erIEndreStatus!!,
                opprinneligStartdato = valgtArenaTiltak.properties.opprinneligstartdato!!,
                opprinneligSluttdato = valgtArenaTiltak.properties.opprinneligsluttdato,
                sluttdato = valgtArenaTiltak.properties.sluttdato,
                startdato = valgtArenaTiltak.properties.startdato!!,
            )
        }
    }
}
