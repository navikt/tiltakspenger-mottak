package no.nav.tpts.mottak.soknad

import io.ktor.routing.Route
import no.nav.tpts.mottak.soknad.soknadList.soknadListRoute

fun Route.soknadRoutes() {
    soknadListRoute()
}
