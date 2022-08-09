package no.nav.tiltakspenger.mottak.søknad

import io.ktor.server.routing.Route
import no.nav.tiltakspenger.mottak.søknad.søknadList.søknadListRoute

fun Route.søknadRoutes() {
    søknadListRoute()
}
