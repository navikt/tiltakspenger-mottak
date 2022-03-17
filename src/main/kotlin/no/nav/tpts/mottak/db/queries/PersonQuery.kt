package no.nav.tpts.mottak.db.queries

import kotliquery.queryOf
import no.nav.tpts.mottak.db.DataSource.session
import org.intellij.lang.annotations.Language

@Language("SQL")
private val insert = "insert into person (ident, fornavn, etternavn) values (:ident, :fornavn, :etternavn)"

object PersonQueries {
    fun insert(ident: String, fornavn: String, etternavn: String) = session.run(
        queryOf(insert, mapOf("ident" to ident, "fornavn" to fornavn, "etternavn" to etternavn)).asUpdate
    )
}
