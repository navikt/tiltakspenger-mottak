package no.nav.tiltakspenger.mottak.søknad

import no.nav.tiltakspenger.mottak.db.PostgresTestcontainer
import no.nav.tiltakspenger.mottak.db.flywayMigrate
import no.nav.tiltakspenger.mottak.db.queries.PersonQueries
import no.nav.tiltakspenger.mottak.søknad.søknadList.Søknad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
internal class InsertSøknadTest {

    companion object {
        @Container
        val postgreSQLContainer = PostgresTestcontainer
    }

    val rawJson = this::class.java.classLoader.getResource("soknad_med_tiltak_fra_arena.json")!!.readText()
    val dokumentInfoId = 321313
    val søknad = Søknad.fromJson(rawJson, "1", "$dokumentInfoId")

    @BeforeAll
    fun setup() {
        flywayMigrate()
    }

    @Test
    fun `should be able to insert and retrieve søknad`() {
        val ident = "123412341"
        PersonQueries.insertIfNotExists(ident, søknad.fornavn, søknad.etternavn)
        SøknadQueries.insertIfNotExists(12317, dokumentInfoId, rawJson, søknad.copy(ident = ident))

        val søknader = SøknadQueries.listSøknader(10, 0, ident)
        val matchingSoknad = søknader.find { it.ident == ident }
        assertNotNull(matchingSoknad)
        assertEquals(0, matchingSoknad?.barnetillegg?.size)
    }

    @Test
    fun `do not insert soknad if it already exists`() {
        val ident = "123412341"
        val journalpostId = 12318
        PersonQueries.insertIfNotExists(ident, søknad.fornavn, søknad.etternavn)
        PersonQueries.insertIfNotExists(ident, søknad.fornavn, søknad.etternavn)
        SøknadQueries.insertIfNotExists(journalpostId, dokumentInfoId, rawJson, søknad.copy(ident = ident))
        try {
            SøknadQueries.insertIfNotExists(journalpostId, dokumentInfoId, rawJson, søknad.copy(ident = ident))
        } catch (e: org.postgresql.util.PSQLException) {
            fail("Exception thrown $e")
        }
    }
}
