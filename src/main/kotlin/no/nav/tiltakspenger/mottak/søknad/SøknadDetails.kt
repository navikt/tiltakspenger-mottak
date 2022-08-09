package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.mottak.søknad.søknadList.Søknad

@Serializable
class SøknadDetails(val søknad: Søknad, val tiltak: Tiltak? = null) {
    companion object {
        fun fromJson(json: String) = SøknadDetails(søknad = Søknad.fromJson(json), tiltak = Tiltak.fromJson(json))
    }
}
