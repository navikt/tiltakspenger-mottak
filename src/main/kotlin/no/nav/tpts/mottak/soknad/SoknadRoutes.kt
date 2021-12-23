package no.nav.tpts.mottak.soknad

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotliquery.Connection
import kotliquery.Row
import kotliquery.Session
import kotliquery.queryOf
import no.nav.tpts.mottak.db.DataSource
import org.intellij.lang.annotations.Language
import java.time.LocalDate
import java.time.ZonedDateTime

fun Route.soknadRoutes() {
    route("/soknad") {
        get {
            @Language("SQL")
            val query = """
                select navn, opprettet_dato, bruker_start_dato, bruker_slutt_dato from soknad
            """.trimIndent()

            val session = Session(Connection(DataSource.dataSource.connection), strict = true)
            val soknader = session.run(queryOf(query).map(Soknad::fromRow).asList)
            call.respond(soknader)
        }
    }
}


@Serializable
data class Soknad(
    val navn: String,
    val opprettetDato: LocalDate,
    val brukerStartDato: ZonedDateTime?,
    val brukerSluttDato: ZonedDateTime?
) {
    companion object {
        fun fromRow(row: Row): Soknad {
            return Soknad(
                navn = row.string("navn"),
                opprettetDato = row.localDate("opprettet_dato"),
                brukerStartDato = row.zonedDateTimeOrNull("bruker_start_dato"),
                brukerSluttDato = row.zonedDateTimeOrNull("bruker_slutt_dato")
            )
        }
    }
}
