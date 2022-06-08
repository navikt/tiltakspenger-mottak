package no.nav.tiltakspenger.mottak.soknad

import io.ktor.server.routing.Route
import no.nav.tiltakspenger.mottak.soknad.soknadList.soknadListRoute

fun Route.soknadRoutes() {
    soknadListRoute()
}
