package no.nav.tpts.mottak.db.queries

import kotliquery.queryOf
import no.nav.tpts.mottak.db.DataSource.session
import org.intellij.lang.annotations.Language

@Language("SQL")
private val personQuery = "select id from person where ident = :ident"

@Language("SQL")
private val insert = "insert into person (ident, fornavn, etternavn) values (:ident, :fornavn, :etternavn)"

object PersonQueries {
    fun getId(ident: String) = session.run(queryOf(personQuery, mapOf("ident" to ident)).map { it.int("id") }.asSingle)
    fun insert(ident: String, fornavn: String, etternavn: String) = session.run(
        queryOf(insert, mapOf("ident" to ident, "fornavn" to fornavn, "etternavn" to etternavn)).asUpdate
    )
}
