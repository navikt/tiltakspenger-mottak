package no.nav.tiltakspenger.mottak.soknad

import kotliquery.Row
import kotliquery.param
import kotliquery.queryOf
import mu.KotlinLogging
import no.nav.tiltakspenger.mottak.db.DataSource.session
import no.nav.tiltakspenger.mottak.soknad.soknadList.Soknad
import no.nav.tpts.mottak.soknad.soknadList.Barnetillegg
import org.intellij.lang.annotations.Language
import org.postgresql.util.PSQLException

object SoknadQueries {
    private val LOG = KotlinLogging.logger {}

    @Language("SQL")
    val soknaderQuery = """
        select p.fornavn, p.etternavn, soknad.dokumentinfo_id, opprettet_dato, bruker_start_dato, bruker_slutt_dato, 
        p.ident, deltar_kvp, deltar_introduksjonsprogrammet, opphold_institusjon, type_institusjon, system_start_dato, 
        system_slutt_dato, tiltak_arrangoer, tiltak_type, b.fornavn barn_fornavn, b.etternavn barn_etternavn, 
        b.bosted barn_bosted, b.alder barn_alder, b.ident barn_ident
        from soknad
        join person p on soknad.ident = p.ident
        left join barnetillegg as b on soknad.dokumentinfo_id = b.dokumentinfo_id 
            and soknad.journalpost_id = b.journalpost_id
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
        values (:ident, :journalpostId, :dokumentInfoId, to_jsonb(:data), :opprettetDato, :brukerStartDato, 
        :brukerSluttDato, :systemStartDato, :systemSluttDato, :deltarKvp, :deltarIntroduksjonsprogrammet,
        :oppholdInstitusjon, :typeInstitusjon, :tiltak_arrangoer, :tiltak_type)
    """.trimIndent()

    @Language("SQL")
    private val existsQuery =
        "select exists(select 1 from soknad where journalpost_id=:journalpostId and dokumentinfo_id=:dokumentInfoId)"

    fun countSoknader() = session.run(queryOf(totalQuery).map { row -> row.int("total") }.asSingle)

    fun listSoknader(pageSize: Int, offset: Int, ident: String?): List<Soknad> {
        val soknader = session.run(
            queryOf(
                soknaderQuery,
                mapOf(
                    "pageSize" to pageSize,
                    "offset" to offset,
                    "ident" to ident.param<String>()
                )
            ).map(Soknad::fromRow).asList
        )
            .groupBy { it.id }
            .map { (_, soknader) ->
                soknader.reduce { acc, soknad -> soknad.copy(barnetillegg = acc.barnetillegg + soknad.barnetillegg) }
            }
        return soknader
    }

    private fun insertSoknad(journalpostId: Int, dokumentInfoId: Int, data: String, soknad: Soknad) {
        session.run(
            queryOf(
                insertQuery,
                mapOf(
                    "ident" to soknad.ident,
                    "journalpostId" to journalpostId,
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

    fun insertIfNotExists(journalpostId: Int, dokumentInfoId: Int, data: String, soknad: Soknad) {
        if (exists(journalpostId, dokumentInfoId)) {
            LOG.info { "Søknad with journalpostId $journalpostId and dokumentInfoId $dokumentInfoId already exists" }
        } else {
            LOG.info { "Insert søknad with journalpostId $journalpostId and dokumentInfoId $dokumentInfoId" }
            insertSoknad(journalpostId, dokumentInfoId, data, soknad)
        }
    }

    private fun exists(journalpostId: Int, dokumentInfoId: Int): Boolean = session.run(
        queryOf(
            existsQuery,
            mapOf("journalpostId" to journalpostId, "dokumentInfoId" to dokumentInfoId)
        ).map { row -> row.boolean("exists") }.asSingle
    ) ?: throw InternalError("Failed to check if soknad exists")
}

fun Soknad.Companion.fromRow(row: Row): Soknad =
    Soknad(
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
        systemRegistrertSluttDato = row.localDateOrNull("system_slutt_dato"),
        barnetillegg = if (row.hasBarnetillegg()) listOf(Barnetillegg.fromRow(row)) else emptyList()
    )

fun Row.hasBarnetillegg(): Boolean {
    return try {
        this.stringOrNull("barn_ident")?.isNotEmpty() ?: false
    } catch (_: PSQLException) {
        false
    }
}

fun Barnetillegg.Companion.fromRow(row: Row): Barnetillegg = Barnetillegg(
    fornavn = row.string("barn_fornavn"),
    etternavn = row.string("barn_etternavn"),
    alder = row.int("barn_alder"),
    bosted = row.string("barn_bosted"),
    ident = row.string("barn_ident"),
)
