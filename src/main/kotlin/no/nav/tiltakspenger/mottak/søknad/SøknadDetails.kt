package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable
import no.nav.tiltakspenger.mottak.søknad.søknadList.Søknad

@Serializable
class SøknadDetails(val søknad: Søknad, val arenaTiltak: ArenaTiltak? = null) {
    companion object {
        fun fromJson(json: String) =
            SøknadDetails(søknad = Søknad.fromJson(json, "", ""), arenaTiltak = ArenaTiltak.fromJson(json))
    }
}
