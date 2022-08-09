package no.nav.tiltakspenger.mottak.søknad.søknadList

import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.coroutines.async
import mu.KotlinLogging
import no.nav.tiltakspenger.mottak.common.pagination.PageData
import no.nav.tiltakspenger.mottak.common.pagination.paginate
import no.nav.tiltakspenger.mottak.søknad.SøknadQueries.countSøknader
import no.nav.tiltakspenger.mottak.søknad.SøknadQueries.listSøknader

private val LOG = KotlinLogging.logger {}

fun Route.søknadListRoute() {
    route("/api/soknad") {
        get {
            val ident = call.request.queryParameters["ident"]
            paginate { offset, pageSize ->
                val total = async { countSøknader() }
                val søknader = async { listSøknader(pageSize = pageSize, offset = offset, ident = ident) }
                return@paginate PageData(data = søknader.await(), total = total.await() ?: 0)
            }
        }
    }.also { LOG.info { "setting up endpoint /api/soknad" } }
}
