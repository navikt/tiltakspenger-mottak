package no.nav.tpts.mottak.soknad

import kotliquery.Row
import kotliquery.queryOf
import no.nav.tpts.mottak.db.DataSource.session
import no.nav.tpts.mottak.soknad.soknadList.Soknad
import org.intellij.lang.annotations.Language

object SoknadQueries {
    @Language("SQL")
    val soknaderByIdentQuery = """
        select p.fornavn, p.etternavn, dokumentinfo_id, opprettet_dato, bruker_start_dato, bruker_slutt_dato, p.ident
        from soknad
        join person p on soknad.ident = p.ident
        where soknad.ident = :ident
        limit :pageSize 
        offset :offset
    """.trimIndent()

    @Language("SQL")
    val soknaderQuery = """
        select p.fornavn, p.etternavn, dokumentinfo_id, opprettet_dato, bruker_start_dato, bruker_slutt_dato, p.ident
        from soknad
        join person p on soknad.ident = p.ident
        limit :pageSize 
        offset :offset
    """.trimIndent()

    @Language("SQL")
    val totalQuery = "select count(*) as total from soknad"

    @Language("SQL")
    private val insertQuery = """
        insert into soknad (ident, journalpost_id,  dokumentinfo_id, data, opprettet_dato, bruker_start_dato, 
        bruker_slutt_dato, system_start_dato, system_slutt_dato) 
        values (:ident, :journalPostId, :dokumentInfoId, to_jsonb(:data), :opprettetDato, :brukerStartDato, 
        :brukerSluttDato, :systemStartDato, :systemSluttDato)
    """.trimIndent()

    fun countSoknader() = session.run(queryOf(totalQuery).map { row -> row.int("total") }.asSingle)

    fun listSoknader(pageSize: Int, offset: Int, ident: String?): List<Soknad> {
        val query = when (ident) {
            null -> soknaderQuery
            else -> soknaderByIdentQuery
        }
        return session.run(
            queryOf(
                query,
                mapOf(
                    "pageSize" to pageSize,
                    "offset" to offset,
                    "ident" to ident
                )
            ).map(::fromRow).asList
        )
    }

    fun insertSoknad(journalPostId: Int?, dokumentInfoId: Int?, data: String, soknad: Soknad) {
        session.run(
            queryOf(
                insertQuery,
                mapOf(
                    "ident" to soknad.ident,
                    "journalPostId" to journalPostId,
                    "dokumentInfoId" to dokumentInfoId,
                    "opprettetDato" to soknad.opprettet,
                    "brukerStartDato" to soknad.brukerRegistrertStartDato,
                    "brukerSluttDato" to soknad.brukerRegistrertSluttDato,
                    "systemStartDato" to soknad.systemRegistrertStartDato,
                    "systemSluttDato" to soknad.systemRegistrertSluttDato,
                    "data" to data,
                )
            ).asUpdate
        )
    }
}

fun fromRow(row: Row): Soknad {
    return Soknad(
        id = row.int("dokumentinfo_id").toString(),
        fornavn = row.string("fornavn"),
        etternavn = row.string("etternavn"),
        ident = row.string("ident"),
        opprettet = row.zonedDateTime("opprettet_dato").toLocalDateTime(),
        brukerRegistrertStartDato = row.localDateOrNull("bruker_start_dato"),
        brukerRegistrertSluttDato = row.localDateOrNull("bruker_slutt_dato"),
        systemRegistrertStartDato = row.localDateOrNull("system_start_dato"),
        systemRegistrertSluttDato = row.localDateOrNull("system_slutt_dato")
    )
}
