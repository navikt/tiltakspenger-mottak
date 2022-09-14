package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.Serializable

@Serializable
class SøknadDetails(val søknad: Søknad, val arenaTiltak: ArenaTiltak? = null) {
    companion object {
        fun fromJson(json: String) =
            SøknadDetails(søknad = Søknad.fromJson(json, "", ""), arenaTiltak = ArenaTiltak.fromJson(json))
    }
}
