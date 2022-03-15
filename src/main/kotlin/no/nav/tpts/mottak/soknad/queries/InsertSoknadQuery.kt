package no.nav.tpts.mottak.soknad.soknadList

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotliquery.queryOf
import no.nav.tpts.mottak.db.DataSource.session
import no.nav.tpts.mottak.joark.models.JoarkSoknad
import no.nav.tpts.mottak.soknad.SoknadDetails
import org.intellij.lang.annotations.Language

@Language("SQL")
private val insertQuery = """
    insert into soknad (soker, journalpost_id,  dokumentinfo_id, data, 
        opprettet_dato, bruker_start_dato, bruker_slutt_dato,
           system_start_dato, system_slutt_dato) values (:soker, :journalPostId, :dokumentInfoId, to_jsonb(:data),
           :opprettetDato, :brukerStartDato, :brukerSluttDato, :systemStartDato, :systemSluttDato)
""".trimIndent()

private val lenientJson = Json {
    ignoreUnknownKeys = true
}

fun SoknadQueries.insertSoknad(journalPostId: Int?, dokumentInfoId: Int?, data: String) {
    val soker = 1 // insert new person if not exists, select id if exists
    val joarkSoknad: JoarkSoknad = lenientJson.decodeFromString(data)
    val soknad = SoknadDetails.fromJoarkSoknad(joarkSoknad)
    session.run(
        queryOf(
            insertQuery,
            mapOf(
                "soker" to soker,
                "journalPostId" to journalPostId,
                "dokumentInfoId" to dokumentInfoId,
                "opprettetDato" to soknad.opprettet,
                "brukerStartDato" to null,
                "brukerSluttDato" to null,
                "systemStartDato" to soknad.tiltak?.opprinneligStartdato,
                "systemSluttDato" to soknad.tiltak?.opprinneligSluttdato,
                "data" to data,
            )
        ).asUpdate
    )
}
