package no.nav.tpts.mottak.soknad

import no.nav.tpts.mottak.db.queries.PersonQueries
import no.nav.tpts.mottak.soknad.soknadList.Soknad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class InsertSoknadTest {

    @BeforeAll
    fun setup() {
        System.setProperty("DB_DATABASE", "postgres")
        System.setProperty("DB_PASSWORD", "postgres")
        System.setProperty("DB_USERNAME", "postgres")
    }

    @Disabled
    @Test
    fun testInsert() {
        val rawJson = this::class.java.classLoader.getResource("soknad_med_tiltak_fra_arena.json")!!.readText()
        val dokumentInfoId = 321312
        val soknad = Soknad.fromJson(rawJson)
        PersonQueries.insertIfNotExists(soknad.ident, soknad.fornavn, soknad.etternavn)
        SoknadQueries.insertSoknad(12312, dokumentInfoId, rawJson, soknad)

        val soknader = SoknadQueries.listSoknader(10, 0, null)
        val matchingSoknad = soknader.firstOrNull { it.ident == soknad.ident }
        assertNotNull(matchingSoknad)
        assertEquals(1, soknad.barnetillegg.size)
    }
}
