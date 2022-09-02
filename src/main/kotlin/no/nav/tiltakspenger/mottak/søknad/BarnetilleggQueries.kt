package no.nav.tiltakspenger.mottak.søknad

import kotliquery.queryOf
import no.nav.tiltakspenger.mottak.db.DataSource.session
import no.nav.tiltakspenger.mottak.søknad.søknadList.Barnetillegg
import org.intellij.lang.annotations.Language

object BarnetilleggQueries {
    fun insertBarnetillegg(barnetillegg: Barnetillegg, journalpostId: Int, dokumentInfoId: Int) {
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
                    "journalpost_id" to journalpostId,
                    "ident" to barnetillegg.ident,
                    "bosted" to barnetillegg.land,
                    "alder" to barnetillegg.alder,
                )
            ).asUpdate
        )
    }
}
