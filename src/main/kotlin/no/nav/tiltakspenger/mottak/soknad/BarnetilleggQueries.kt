package no.nav.tiltakspenger.mottak.soknad

import kotliquery.Row
import kotliquery.queryOf
import no.nav.tiltakspenger.mottak.db.DataSource.session
import no.nav.tpts.mottak.soknad.soknadList.Barnetillegg
import org.intellij.lang.annotations.Language
import org.postgresql.util.PSQLException

object BarnetilleggQueries {
    fun insertBarnetillegg(barnetillegg: Barnetillegg, journalPostId: Int, dokumentInfoId: Int) {
        @Language("SQL")
        val query = """
            INSERT INTO barnetillegg (dokumentinfo_id, journalpost_id, ident, fornavn, etternavn, alder, bosted) 
            VALUES (:dokumentinfo_id, :journalpost_id, :ident, :fornavn, :etternavn, :alder, :bosted)
        """.trimIndent()
        session.run(
            queryOf(
                query,
                mapOf(
                    "dokumentinfo_id" to dokumentInfoId,
                    "journalpost_id" to journalPostId,
                    "ident" to barnetillegg.ident,
                    "bosted" to barnetillegg.bosted,
                    "fornavn" to barnetillegg.fornavn,
                    "etternavn" to barnetillegg.etternavn,
                    "alder" to barnetillegg.alder,
                )
            ).asUpdate
        )
    }
}

fun Row.hasBarnetillegg(): Boolean {
    return try {
        this.string("barnetillegg.ident").isNotEmpty()
    } catch (e: PSQLException) {
        false
    }
}

fun Barnetillegg.Companion.fromRow(row: Row): Barnetillegg = Barnetillegg(
    fornavn = row.string("barnetillegg.fornavn"),
    etternavn = row.string("barnetillegg.etternavn"),
    alder = row.int("barnetillegg.alder"),
    bosted = row.string("barnetillegg.bosted"),
    ident = row.string("barnetillegg.ident"),
)
