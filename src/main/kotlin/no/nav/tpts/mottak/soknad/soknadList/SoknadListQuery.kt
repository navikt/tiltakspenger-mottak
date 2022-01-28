package no.nav.tpts.mottak.soknad.soknadList

import kotliquery.Row
import org.intellij.lang.annotations.Language

@Language("SQL")
val soknadListQuery = """
    select navn, dokumentinfo_id, opprettet_dato, bruker_start_dato, bruker_slutt_dato, 
        concat_ws(' ', i.ident) as identer
    from soknad 
    join person p on soknad.soker = p.id
    join public.ident i on p.id = i.person_id
    limit :pageSize offset :offset
""".trimIndent()

@Language("SQL")
val totalQuery = "select count(*) as total from soknad"

fun Soknad.Companion.fromRow(row: Row): Soknad {
    return Soknad(
        id = row.int("dokumentinfo_id").toString(),
        fornavn = row.string("navn").split(" ").first(),
        etternavn = row.string("navn").split(" ").drop(1).joinToString(" "),
        ident = row.string("identer").split(" ").first(),
        opprettet = row.zonedDateTime("opprettet_dato").toLocalDateTime(),
        brukerStartDato = row.localDateOrNull("bruker_start_dato"),
        brukerSluttDato = row.localDateOrNull("bruker_slutt_dato")
    )
}
