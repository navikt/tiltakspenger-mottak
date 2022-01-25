package no.nav.tpts.mottak.soknad

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import kotlinx.coroutines.async
import kotliquery.Row
import kotliquery.queryOf
import no.nav.tpts.mottak.common.http.badRequest
import no.nav.tpts.mottak.common.http.notFound
import no.nav.tpts.mottak.common.pagination.PageData
import no.nav.tpts.mottak.common.pagination.paginate
import no.nav.tpts.mottak.db.DataSource
import org.intellij.lang.annotations.Language

@Language("SQL")
val totalQuery = "select count(*) as total from soknad"

const val DEFAULT_PAGE_SIZE = 20

fun Route.soknadRoutes() {
    route("/api/soknad") {
        get {
            paginate { offset, pageSize ->
                @Language("SQL")
                val query = """
                select navn, dokumentinfo_id, opprettet_dato, bruker_start_dato, bruker_slutt_dato, 
                    concat_ws(' ', i.ident) as identer
                from soknad 
                join person p on soknad.soker = p.id
                join public.ident i on p.id = i.person_id
                limit :pageSize offset :offset
                """.trimIndent()
                val total =
                    async { DataSource.session.run(queryOf(totalQuery).map { row -> row.int("total") }.asSingle) }
                val soknader = async {
                    DataSource.session.run(
                        queryOf(
                            query,
                            mapOf(
                                "pageSize" to pageSize,
                                "offset" to offset
                            )
                        ).map(Soknad::fromRow).asList
                    )
                }
                return@paginate PageData<Soknad>(data = soknader.await(), total = total.await() ?: 0)
            }
        }

        get("/{dokumentInfoId}") {
            val dokumentInfoId =
                call.parameters["dokumentInfoId"]?.toIntOrNull() ?: return@get call.badRequest("SoknadId missing")

            @Language("SQL")
            val query = """
                select navn, opprettet_dato, bruker_start_dato, bruker_slutt_dato 
                from soknad 
                where dokumentinfo_id = :dokumentInfoId
            """.trimIndent()
            val soknad = DataSource.session.run(
                queryOf(
                    query,
                    mapOf("dokumentInfoId" to dokumentInfoId.toInt())
                ).map(SoknadDetails::fromRow).asSingle
            ) ?: return@get call.notFound("Soknad with id $dokumentInfoId not found")
            call.respond(soknad)
        }
    }
}

fun Soknad.Companion.fromRow(row: Row): Soknad {
    return Soknad(
        id = row.int("dokumentinfo_id").toString(),
        fornavn = row.string("navn").split(" ").first(),
        etternavn = row.string("navn").split(" ").drop(1).joinToString(" "),
        identer = row.string("identer").split(" "),
        opprettetDato = row.zonedDateTime("opprettet_dato").toLocalDateTime(),
        brukerStartDato = row.localDateOrNull("bruker_start_dato"),
        brukerSluttDato = row.localDateOrNull("bruker_slutt_dato")
    )
}

fun SoknadDetails.Companion.fromRow(row: Row): SoknadDetails {
    return SoknadDetails(
        fornavn = row.string("navn").split(" ").first(),
        etternavn = row.string("navn").split(" ").drop(1).joinToString(" "),
        opprettetDato = row.zonedDateTime("opprettet_dato").toLocalDateTime(),
        brukerStartDato = row.localDateOrNull("bruker_start_dato"),
        brukerSluttDato = row.localDateOrNull("bruker_slutt_dato")
    )
}
