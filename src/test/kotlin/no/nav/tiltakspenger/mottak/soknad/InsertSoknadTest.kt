package no.nav.tiltakspenger.mottak.soknad

import no.nav.tiltakspenger.mottak.db.TestPostgresqlContainer
import no.nav.tiltakspenger.mottak.db.flywayMigrate
import no.nav.tiltakspenger.mottak.db.queries.PersonQueries
import no.nav.tiltakspenger.mottak.soknad.soknadList.Soknad
import no.nav.tpts.mottak.soknad.soknadList.Barnetillegg
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
internal class InsertSoknadTest {

    companion object {
        @Container
        @JvmField
        val postgreSQLContainer: TestPostgresqlContainer = TestPostgresqlContainer.instance
    }

    val rawJson = this::class.java.classLoader.getResource("soknad_med_tiltak_fra_arena.json")!!.readText()
    val dokumentInfoId = 321313
    val soknad = Soknad.fromJson(rawJson)

    @BeforeAll
    fun setup() {
        flywayMigrate()
    }

    @Test
    fun `should be able to insert and retrieve søknad`() {
        val ident = "123412341"
        PersonQueries.insertIfNotExists(ident, soknad.fornavn, soknad.etternavn)
        SoknadQueries.insertIfNotExists(12317, dokumentInfoId, rawJson, soknad.copy(ident = ident))

        val soknader = SoknadQueries.listSoknader(10, 0, ident)
        val matchingSoknad = soknader.find { it.ident == ident }
        assertNotNull(matchingSoknad)
        assertEquals(0, matchingSoknad?.barnetillegg?.size)
    }

    @Test
    fun `do not insert soknad if it already exists`() {
        val ident = "123412341"
        val journalpostId = 12318
        PersonQueries.insertIfNotExists(ident, soknad.fornavn, soknad.etternavn)
        PersonQueries.insertIfNotExists(ident, soknad.fornavn, soknad.etternavn)
        SoknadQueries.insertIfNotExists(journalpostId, dokumentInfoId, rawJson, soknad.copy(ident = ident))
        try {
            SoknadQueries.insertIfNotExists(journalpostId, dokumentInfoId, rawJson, soknad.copy(ident = ident))
        } catch (e: org.postgresql.util.PSQLException) {
            fail("Exception thrown $e")
        }
    }

    @Test
    fun `should handle 2 barnetillegg`() {
        val ident = "123412342"
        val journalpostId = 12318
        PersonQueries.insertIfNotExists(ident, soknad.fornavn, soknad.etternavn)
        SoknadQueries.insertIfNotExists(journalpostId, dokumentInfoId, rawJson, soknad.copy(ident = ident))
        BarnetilleggQueries.insertBarnetillegg(
            Barnetillegg(
                fornavn = "Sig",
                etternavn = "Grø",
                alder = 15,
                ident = "123412345",
                bosted = "Førde"
            ),
            journalpostId,
            dokumentInfoId
        )
        BarnetilleggQueries.insertBarnetillegg(
            Barnetillegg(
                fornavn = "Sig",
                etternavn = "Grø",
                alder = 15,
                ident = "123412346",
                bosted = "Førde"
            ),
            journalpostId,
            dokumentInfoId
        )

        val soknader = SoknadQueries.listSoknader(10, 0, null)
        val matchingSoknad = soknader.find { it.ident == ident }
        assertNotNull(matchingSoknad)
        assertEquals(2, matchingSoknad?.barnetillegg?.size)
    }
}
