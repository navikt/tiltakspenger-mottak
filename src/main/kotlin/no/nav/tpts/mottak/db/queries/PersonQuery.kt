package no.nav.tpts.mottak.db.queries

import kotliquery.queryOf
import no.nav.tpts.mottak.db.DataSource.session
import org.intellij.lang.annotations.Language

@Language("SQL")
private val insert = "insert into person (ident, fornavn, etternavn) values (:ident, :fornavn, :etternavn)"

@Language("SQL")
private val existsQuery = "select exists(select 1 from person where ident =:ident)"

object PersonQueries {
    private fun insert(ident: String, fornavn: String, etternavn: String) = session.run(
        queryOf(insert, mapOf("ident" to ident, "fornavn" to fornavn, "etternavn" to etternavn)).asUpdate
    )

    private fun exists(ident: String): Boolean = session.run(
        queryOf(existsQuery, mapOf("ident" to ident)).map { row -> row.boolean("exists") }.asSingle
    ) ?: throw InternalError("Failed to check if person exists")

    fun insertIfNotExists(ident: String, fornavn: String, etternavn: String) {
        if (exists(ident))
            return
        else
            insert(ident, fornavn, etternavn)
    }
}
