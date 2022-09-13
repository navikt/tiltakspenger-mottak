package no.nav.tiltakspenger.mottak.søknad

import kotliquery.Row
import kotliquery.param
import kotliquery.queryOf
import mu.KotlinLogging
import no.nav.tiltakspenger.mottak.db.DataSource.session
import no.nav.tiltakspenger.mottak.søknad.søknadList.Barnetillegg
import no.nav.tiltakspenger.mottak.søknad.søknadList.Søknad
import org.intellij.lang.annotations.Language
import org.postgresql.util.PSQLException

object SøknadQueries {
    private val LOG = KotlinLogging.logger {}

    @Language("SQL")
    val søknaderQuery = """
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

    fun countSøknader() = session.run(queryOf(totalQuery).map { row -> row.int("total") }.asSingle)

    fun listSøknader(pageSize: Int, offset: Int, ident: String?): List<Søknad> {
        val søknader = session.run(
            queryOf(
                søknaderQuery,
                mapOf(
                    "pageSize" to pageSize,
                    "offset" to offset,
                    "ident" to ident.param<String>()
                )
            ).map(Søknad::fromRow).asList
        )
            .groupBy { it.søknadId }
            .map { (_, søknader) ->
                søknader.reduce { acc, soknad -> soknad.copy(barnetillegg = acc.barnetillegg + soknad.barnetillegg) }
            }
        return søknader
    }

    private fun insertSøknad(journalpostId: Int, dokumentInfoId: Int, data: String, søknad: Søknad) {
        session.run(
            queryOf(
                insertQuery,
                mapOf(
                    "ident" to søknad.ident,
                    "journalpostId" to journalpostId,
                    "dokumentInfoId" to dokumentInfoId,
                    "opprettetDato" to søknad.opprettet,
                    "brukerStartDato" to søknad.brukerregistrertTiltak?.fom,
                    "brukerSluttDato" to søknad.brukerregistrertTiltak?.tom,
                    "systemStartDato" to søknad.arenaTiltak?.opprinneligStartdato,
                    "systemSluttDato" to søknad.arenaTiltak?.opprinneligSluttdato,
                    "data" to data,
                    "deltarKvp" to søknad.deltarKvp,
                    "deltarIntroduksjonsprogrammet" to søknad.deltarIntroduksjonsprogrammet,
                    "oppholdInstitusjon" to søknad.oppholdInstitusjon,
                    "type_institusjon" to søknad.typeInstitusjon,
                    "tiltak_arrangoer" to søknad.arenaTiltak?.arrangoer,
                    "tiltak_type" to søknad.arenaTiltak?.tiltakskode
                )
            ).asUpdate
        )
    }

    fun insertIfNotExists(journalpostId: Int, dokumentInfoId: Int, data: String, søknad: Søknad) {
        if (exists(journalpostId, dokumentInfoId)) {
            LOG.info { "Søknad with journalpostId $journalpostId and dokumentInfoId $dokumentInfoId already exists" }
        } else {
            LOG.info { "Insert søknad with journalpostId $journalpostId and dokumentInfoId $dokumentInfoId" }
            insertSøknad(journalpostId, dokumentInfoId, data, søknad)
        }
    }

    private fun exists(journalpostId: Int, dokumentInfoId: Int): Boolean = session.run(
        queryOf(
            existsQuery,
            mapOf("journalpostId" to journalpostId, "dokumentInfoId" to dokumentInfoId)
        ).map { row -> row.boolean("exists") }.asSingle
    ) ?: throw InternalError("Failed to check if soknad exists")
}

fun Søknad.Companion.fromRow(row: Row): Søknad =
    Søknad(
        søknadId = row.int("dokumentinfo_id").toString(),
        journalpostId = "",
        dokumentInfoId = "",
        fornavn = row.string("fornavn"),
        etternavn = row.string("etternavn"),
        ident = row.string("ident"),
        deltarKvp = row.boolean("deltar_kvp"),
        deltarIntroduksjonsprogrammet = row.boolean("deltar_introduksjonsprogrammet"),
//        tiltaksArrangoer = row.stringOrNull("tiltak_arrangoer"),
//        tiltaksType = row.stringOrNull("tiltak_type"),
        oppholdInstitusjon = row.boolean("opphold_institusjon"),
//        brukerRegistrertStartDato = row.localDateOrNull("bruker_start_dato"),
//        brukerRegistrertSluttDato = row.localDateOrNull("bruker_slutt_dato"),
//        systemRegistrertStartDato = row.localDateOrNull("system_start_dato"),
//        systemRegistrertSluttDato = row.localDateOrNull("system_slutt_dato"),
        typeInstitusjon = row.stringOrNull("type_institusjon"),
        opprettet = row.zonedDateTime("opprettet_dato").toLocalDateTime(),
        barnetillegg = if (row.hasBarnetillegg()) listOf(Barnetillegg.fromRow(row)) else emptyList(),
        arenaTiltak = null,
        brukerregistrertTiltak = null
    )

fun Row.hasBarnetillegg(): Boolean {
    return try {
        this.stringOrNull("barn_ident")?.isNotEmpty() ?: false
    } catch (_: PSQLException) {
        false
    }
}

fun Barnetillegg.Companion.fromRow(row: Row): Barnetillegg = Barnetillegg(
    ident = row.string("barn_ident"),
    alder = row.int("barn_alder"),
    land = row.string("barn_bosted"),
)
