package no.nav.tpts.mottak.soknad.soknadList

import kotliquery.Row
import kotliquery.queryOf
import no.nav.tpts.mottak.db.DataSource.session
import org.intellij.lang.annotations.Language

@Language("SQL")
val soknadListQuery = """
    select p.fornavn, p.etternavn, dokumentinfo_id, opprettet_dato, bruker_start_dato, bruker_slutt_dato, p.ident
    from soknad
    join person p on soknad.soker = p.id
    limit :pageSize offset :offset
""".trimIndent()

@Language("SQL")
val totalQuery = "select count(*) as total from soknad"

object SoknadQueries {
    fun countSoknader() = session.run(queryOf(totalQuery).map { row -> row.int("total") }.asSingle)
    fun listSoknader(pageSize: Int, offset: Int): List<Soknad> {
        return session.run(
            queryOf(
                soknadListQuery,
                mapOf(
                    "pageSize" to pageSize,
                    "offset" to offset
                )
            ).map(Soknad::fromRow).asList
        )
    }
}

fun Soknad.Companion.fromRow(row: Row): Soknad {
    return Soknad(
        id = row.int("dokumentinfo_id").toString(),
        fornavn = row.stringOrNull("fornavn"),
        etternavn = row.string("etternavn"),
        ident = row.string("ident"),
        opprettet = row.zonedDateTime("opprettet_dato").toLocalDateTime(),
        brukerRegistrertStartDato = row.localDateOrNull("bruker_start_dato"),
        brukerRegistrertSluttDato = row.localDateOrNull("bruker_slutt_dato"),
        systemRegistrertStartDato = null,
        systemRegistrertSluttDato = null
    )
}
