package no.nav.tiltakspenger.mottak.db.queries

import kotliquery.queryOf
import mu.KotlinLogging
import no.nav.tiltakspenger.mottak.db.DataSource.session
import org.intellij.lang.annotations.Language

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

object PersonQueries {

    @Language("SQL")
    private val insert = "insert into person (ident, fornavn, etternavn) values (:ident, :fornavn, :etternavn)"

    @Language("SQL")
    private val existsQuery = "select exists(select 1 from person where ident=:ident)"

    private fun insert(ident: String, fornavn: String, etternavn: String) = session.run(
        queryOf(insert, mapOf("ident" to ident, "fornavn" to fornavn, "etternavn" to etternavn)).asUpdate
    )

    private fun exists(ident: String): Boolean = session.run(
        queryOf(existsQuery, mapOf("ident" to ident)).map { row -> row.boolean("exists") }.asSingle
    ) ?: throw InternalError("Failed to check if person exists")

    fun insertIfNotExists(ident: String, fornavn: String?, etternavn: String?) {
        if (exists(ident)) {
            LOG.info { "User already exists" }
            SECURELOG.info { "User $etternavn already exists" }
            return
        } else {
            LOG.info { "Insert user" }
            SECURELOG.info { "Insert user $etternavn" }
            insert(ident, fornavn ?: "", etternavn ?: "")
        }
    }
}
