package no.nav.tpts.mottak.soknad.soknadList

import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import kotlinx.coroutines.async
import kotliquery.queryOf
import no.nav.tpts.mottak.common.pagination.PageData
import no.nav.tpts.mottak.common.pagination.paginate
import no.nav.tpts.mottak.db.DataSource

fun Route.soknadListRoute() {
    route("/api/soknad") {
        get {
            paginate { offset, pageSize ->
                val total =
                    async { DataSource.session.run(queryOf(totalQuery).map { row -> row.int("total") }.asSingle) }
                val soknader = async {
                    DataSource.session.run(
                        queryOf(
                            soknadListQuery,
                            mapOf(
                                "pageSize" to pageSize,
                                "offset" to offset
                            )
                        ).map(Soknad::fromRow).asList
                    )
                }
                return@paginate PageData(data = soknader.await(), total = total.await() ?: 0)
            }
        }
    }
}
