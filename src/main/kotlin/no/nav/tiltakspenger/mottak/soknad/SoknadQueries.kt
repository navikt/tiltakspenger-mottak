package no.nav.tiltakspenger.mottak.soknad

import kotliquery.Row
import kotliquery.param
import kotliquery.queryOf
import no.nav.tiltakspenger.mottak.db.DataSource.session
import no.nav.tiltakspenger.mottak.soknad.soknadList.Soknad
import org.intellij.lang.annotations.Language

object SoknadQueries {
    @Language("SQL")
    val soknaderQuery = """
        select p.fornavn, p.etternavn, dokumentinfo_id, opprettet_dato, bruker_start_dato, bruker_slutt_dato, p.ident, 
        deltar_kvp, deltar_introduksjonsprogrammet, opphold_institusjon, type_institusjon, system_start_dato, 
        system_slutt_dato, tiltak_arrangoer, tiltak_type
        from soknad
        join person p on soknad.ident = p.ident
        where :ident IS NULL or soknad.ident = :ident 
        limit :pageSize 
        offset :offset
    """.trimIndent()

    @Language("SQL")
    val totalQuery = "select count(*) as total from soknad"

    @Language("SQL")
    private val insertQuery = """
        insert into soknad (ident, journalpost_id,  dokumentinfo_id, data, opprettet_dato, bruker_start_dato, 
        bruker_slutt_dato, system_start_dato, system_slutt_dato, deltar_kvp, deltar_introduksjonsprogrammet, 
        opphold_institusjon, type_institusjon, tiltak_arrangoer, tiltak_type) 
        values (:ident, :journalPostId, :dokumentInfoId, to_jsonb(:data), :opprettetDato, :brukerStartDato, 
        :brukerSluttDato, :systemStartDato, :systemSluttDato, :deltarKvp, :deltarIntroduksjonsprogrammet,
        :oppholdInstitusjon, :typeInstitusjon, :tiltak_arrangoer, :tiltak_type)
    """.trimIndent()

    fun countSoknader() = session.run(queryOf(totalQuery).map { row -> row.int("total") }.asSingle)

    fun listSoknader(pageSize: Int, offset: Int, ident: String?): List<Soknad> {
        return session.run(
            queryOf(
                soknaderQuery,
                mapOf(
                    "pageSize" to pageSize,
                    "offset" to offset,
                    "ident" to ident.param<String>()
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
                    "deltarKvp" to soknad.deltarKvp,
                    "deltarIntroduksjonsprogrammet" to soknad.deltarIntroduksjonsprogrammet,
                    "oppholdInstitusjon" to soknad.oppholdInstitusjon,
                    "type_institusjon" to soknad.typeInstitusjon,
                    "tiltak_arrangoer" to soknad.tiltaksArrangoer,
                    "tiltak_type" to soknad.tiltaksType
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
        deltarKvp = row.boolean("deltar_kvp"),
        deltarIntroduksjonsprogrammet = row.boolean("deltar_introduksjonsprogrammet"),
        oppholdInstitusjon = row.boolean("opphold_institusjon"),
        typeInstitusjon = row.stringOrNull("type_institusjon"),
        tiltaksArrangoer = row.stringOrNull("tiltak_arrangoer"),
        tiltaksType = row.stringOrNull("tiltak_type"),
        opprettet = row.zonedDateTime("opprettet_dato").toLocalDateTime(),
        brukerRegistrertStartDato = row.localDateOrNull("bruker_start_dato"),
        brukerRegistrertSluttDato = row.localDateOrNull("bruker_slutt_dato"),
        systemRegistrertStartDato = row.localDateOrNull("system_start_dato"),
        systemRegistrertSluttDato = row.localDateOrNull("system_slutt_dato")
    )
}
