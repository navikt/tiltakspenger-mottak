package no.nav.tpts.mottak.soknad.soknadList

import kotlinx.serialization.json.Json
import kotliquery.queryOf
import no.nav.tpts.mottak.db.DataSource.session
import org.intellij.lang.annotations.Language

@Language("SQL")
private val insertQuery = """
    insert into soknad (ident, journalpost_id,  dokumentinfo_id, data, 
        opprettet_dato, bruker_start_dato, bruker_slutt_dato,
           system_start_dato, system_slutt_dato) values (:ident, :journalPostId, :dokumentInfoId, to_jsonb(:data),
           :opprettetDato, :brukerStartDato, :brukerSluttDato, :systemStartDato, :systemSluttDato)
""".trimIndent()

val lenientJson = Json {
    ignoreUnknownKeys = true
}

fun SoknadQueries.insertSoknad(journalPostId: Int?, dokumentInfoId: Int?, data: String, soknad: Soknad) {
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
