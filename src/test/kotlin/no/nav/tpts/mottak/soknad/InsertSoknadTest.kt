package no.nav.tpts.mottak.soknad

import no.nav.tpts.mottak.db.queries.PersonQueries
import no.nav.tpts.mottak.soknad.soknadList.Soknad
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class InsertSoknadTest {

    @BeforeAll
    fun setup() {
        System.setProperty("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_DATABASE", "postgres")
        System.setProperty("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_PASSWORD", "postgres")
        System.setProperty("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_USERNAME", "postgres")
    }

    @Disabled
    @Test
    fun testInsert() {
        val rawJson = this::class.java.classLoader.getResource("faktumsSkjermet.json")!!.readText()
        val dokumentInfoId = 321312
        val soknad = Soknad.fromJson(rawJson)
        PersonQueries.insertIfNotExists(soknad.ident, soknad.fornavn, soknad.etternavn)
        SoknadQueries.insertSoknad(12312, dokumentInfoId, rawJson, soknad)

        val soknader = SoknadQueries.listSoknader(10, 0, null)
        val matchingSoknad = soknader.firstOrNull { it.ident == soknad.ident }
        assertNotNull(matchingSoknad)
    }
}
