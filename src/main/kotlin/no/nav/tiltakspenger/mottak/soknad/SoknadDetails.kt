package no.nav.tiltakspenger.mottak.soknad

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.mottak.soknad.soknadList.Soknad

@Serializable
class SoknadDetails(val soknad: Soknad, val tiltak: Tiltak? = null) {
    companion object {
        fun fromJson(json: String) = SoknadDetails(soknad = Soknad.fromJson(json), tiltak = Tiltak.fromJson(json))
    }
}
