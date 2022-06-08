package no.nav.tiltakspenger.mottak.soknad

import no.nav.tiltakspenger.mottak.db.DataSource
import no.nav.tiltakspenger.mottak.db.queries.PersonQueries
import no.nav.tiltakspenger.mottak.soknad.soknadList.Soknad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class InsertSoknadTest {

    @BeforeAll
    fun setup() {
        System.setProperty(DataSource.DB_DATABASE_KEY, "postgres")
        System.setProperty(DataSource.DB_PASSWORD_KEY, "postgres")
        System.setProperty(DataSource.DB_USERNAME_KEY, "postgres")
        System.setProperty(DataSource.DB_HOST_KEY, "localhost")
        System.setProperty(DataSource.DB_PORT_KEY, "5432")
    }

    @Test
    fun testInsert() {
        val rawJson = this::class.java.classLoader.getResource("soknad_med_tiltak_fra_arena.json")!!.readText()
        val dokumentInfoId = 321313
        val soknad = Soknad.fromJson(rawJson)
        PersonQueries.insertIfNotExists(soknad.ident, soknad.fornavn, soknad.etternavn)
        SoknadQueries.insertSoknad(12314, dokumentInfoId, rawJson, soknad)

        val soknader = SoknadQueries.listSoknader(10, 0, null)
        val matchingSoknad = soknader.firstOrNull { it.ident == soknad.ident }
        assertNotNull(matchingSoknad)
        assertEquals(1, soknad.barnetillegg.size)
    }
}
