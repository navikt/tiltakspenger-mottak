package no.nav.tpts.mottak.soknad.soknadList

import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import kotlinx.coroutines.async
import no.nav.tpts.mottak.common.pagination.PageData
import no.nav.tpts.mottak.common.pagination.paginate

fun Route.soknadListRoute() {
    route("/api/soknad") {
        get {
            val ident = call.request.queryParameters["ident"]

            paginate { offset, pageSize ->
                val total = async { SoknadQueries.countSoknader() }
                val soknader = async { SoknadQueries.listSoknader(pageSize = pageSize, offset = offset, ident = ident) }

                return@paginate PageData(data = soknader.await(), total = total.await() ?: 0)
            }
        }
    }
}
