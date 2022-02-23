package no.nav.tpts.mottak.soknad.soknadList

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotliquery.queryOf
import no.nav.tpts.mottak.db.DataSource.session
import no.nav.tpts.mottak.joark.models.JoarkSoknad
import org.intellij.lang.annotations.Language

@Language("SQL")
val insertQuery: String = """insert into soknad (soker, journalpost_id,  dokumentinfo_id, data, navn, 
        opprettet_dato, bruker_start_dato, bruker_slutt_dato,
           system_start_dato, system_slutt_dato) values (1, ?, ?, ?, ? ,?, ?, ?)""".trimIndent()

fun insertSoknad(journalPostId: Int?, dokumentInfoId: Int?, data: String) {
    val joarkSoknad: JoarkSoknad = Json {
        ignoreUnknownKeys = true
    }.decodeFromString(data)

    session.run(queryOf(insertQuery, journalPostId, dokumentInfoId, data, hentPersonNavn(joarkSoknad)).asUpdate)
}

fun hentPersonNavn(joarkSoknad: JoarkSoknad) =
    joarkSoknad.fakta.first { faktum -> faktum.key == "personalia" }.properties?.navn


