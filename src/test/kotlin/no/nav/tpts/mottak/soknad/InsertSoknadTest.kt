package no.nav.tpts.mottak.soknad

import no.nav.tpts.mottak.soknad.soknadList.SoknadQueries
import no.nav.tpts.mottak.soknad.soknadList.insertSoknad
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class InsertSoknadTest {

    @BeforeAll
    fun setup() {
        System.setProperty("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_DATABASE", "postgres")
        System.setProperty("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_PASSWORD", "postgres")
        System.setProperty("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_USERNAME", "postgres")
    }

    @Test
    fun testInsert() {
        val rawJson = this::class.java.classLoader.getResource("faktumsSkjermet.json")!!.readText()
        val dokumentInfoId = 321312
        SoknadQueries.insertSoknad(12312, dokumentInfoId, rawJson)

        val soknader = SoknadQueries.listSoknader(10, 0)
        val matchingSoknad = soknader.firstOrNull() { it.id === dokumentInfoId.toString() }
        assertNotNull(matchingSoknad)
    }
}
