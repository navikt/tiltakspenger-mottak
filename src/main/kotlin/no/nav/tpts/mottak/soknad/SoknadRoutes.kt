package no.nav.tpts.mottak.soknad

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import kotliquery.Row
import kotliquery.queryOf
import no.nav.tpts.mottak.db.DataSource
import org.intellij.lang.annotations.Language

fun Route.soknadRoutes() {
    route("/soknad") {
        get {
            @Language("SQL")
            val query = """
                select navn, opprettet_dato, bruker_start_dato, bruker_slutt_dato from soknad
            """.trimIndent()
            val soknader = DataSource.session.run(queryOf(query).map(Soknad::fromRow).asList)
            call.respond(soknader)
        }
    }
}

fun Soknad.Companion.fromRow(row: Row): Soknad {
    return Soknad(
        navn = row.string("navn"),
        opprettetDato = row.zonedDateTime("opprettet_dato").toLocalDateTime(),
        brukerStartDato = row.localDateOrNull("bruker_start_dato"),
        brukerSluttDato = row.localDateOrNull("bruker_slutt_dato")
    )
}
