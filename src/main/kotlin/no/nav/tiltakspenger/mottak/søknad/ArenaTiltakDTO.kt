package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import no.nav.tiltakspenger.mottak.serder.StrictLocalDateSerializer
import no.nav.tiltakspenger.mottak.søknad.models.JoarkSøknad
import java.time.LocalDate

@Serializable
data class ArenaTiltakDTO(
    val arenaId: String,
    val arrangoernavn: String?, // kan være null, f.eks. "Utvidet oppfølging i NAV"
    val tiltakskode: String,
    @Serializable(with = StrictLocalDateSerializer::class) val opprinneligSluttdato: LocalDate? = null,
    @Serializable(with = StrictLocalDateSerializer::class) val opprinneligStartdato: LocalDate? = null,
    @Serializable(with = StrictLocalDateSerializer::class) val sluttdato: LocalDate? = null,
    @Serializable(with = StrictLocalDateSerializer::class) val startdato: LocalDate,
) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }

        fun fromJson(json: String): ArenaTiltakDTO? = fromV1Soknad(this.json.decodeFromString(json))

        fun fromV1Soknad(joarkSøknad: JoarkSøknad): ArenaTiltakDTO? {
            val valgtTiltakId = joarkSøknad.fakta.first { it.key == "tiltaksliste.valgtTiltak" }.value
            val valgtArenaTiltak = joarkSøknad.fakta.firstOrNull {
                it.key == "tiltaksliste.tiltakFraArena" && it.faktumId.toString() == valgtTiltakId
            } ?: return null
            return ArenaTiltakDTO(
                arenaId = valgtArenaTiltak.properties.arenaId!!,
                arrangoernavn = valgtArenaTiltak.properties.arrangoer,
                tiltakskode = valgtArenaTiltak.value!!,
                opprinneligSluttdato = valgtArenaTiltak.properties.opprinneligsluttdato,
                opprinneligStartdato = valgtArenaTiltak.properties.opprinneligstartdato!!,
                sluttdato = valgtArenaTiltak.properties.sluttdato,
                startdato = valgtArenaTiltak.properties.startdato!!,
            )
        }
    }
}
