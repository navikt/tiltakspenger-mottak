package no.nav.tiltakspenger.mottak.soknad

import no.nav.tiltakspenger.mottak.db.DataSource
import no.nav.tiltakspenger.mottak.db.queries.PersonQueries
import no.nav.tiltakspenger.mottak.soknad.soknadList.Soknad
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
