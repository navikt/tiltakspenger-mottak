package no.nav.tiltakspenger.mottak.soknad

import no.nav.tiltakspenger.mottak.db.DataSource
import no.nav.tiltakspenger.mottak.db.TestPostgresqlContainer
import no.nav.tiltakspenger.mottak.db.flywayMigrate
import no.nav.tiltakspenger.mottak.db.queries.PersonQueries
import no.nav.tiltakspenger.mottak.soknad.soknadList.Soknad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
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

    @Test
    fun testInsert() {
        val rawJson = this::class.java.classLoader.getResource("soknad_med_tiltak_fra_arena.json")!!.readText()
        val dokumentInfoId = 321313
        val soknad = Soknad.fromJson(rawJson)

        flywayMigrate()
        PersonQueries.insertIfNotExists(soknad.ident, soknad.fornavn, soknad.etternavn)
        SoknadQueries.insertSoknad(12317, dokumentInfoId, rawJson, soknad)

        val soknader = SoknadQueries.listSoknader(10, 0, null)
        val matchingSoknad = soknader.firstOrNull { it.ident == soknad.ident }
        assertNotNull(matchingSoknad)
        assertEquals(0, matchingSoknad?.barnetillegg?.size)
    }
}
