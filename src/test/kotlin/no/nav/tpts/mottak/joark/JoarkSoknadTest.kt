package no.nav.tpts.mottak.joark

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.nav.tpts.mottak.joark.models.JoarkSoknad
import no.nav.tpts.mottak.soknad.SoknadDetails
import no.nav.tpts.mottak.soknad.soknadList.Soknad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.time.LocalDate

internal class JoarkSoknadTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `deserialize soknad`() {
        val rawJsonSoknad = javaClass.getResource("/mocksoknad.json")?.readText(Charsets.UTF_8)!!
        json.decodeFromString<JoarkSoknad>(rawJsonSoknad)
    }

    @Test
    fun `should put joark faktum data into soknad object`() {
        val faktums = this::class.java.classLoader.getResource("faktumsSkjermet.json")!!.readText()
        SoknadDetails.fromJson(faktums).also {
            assertEquals("BRÅKETE", it.soknad.fornavn)
            assertEquals("BLYANT", it.soknad.etternavn)
            assertEquals("14038205537", it.soknad.ident)
            assertEquals("2022-03-10T10:03:35.365", it.soknad.opprettet.toString())
            assertEquals("136950219", it.tiltak?.id)
            assertEquals("Arbeidsrettet rehabilitering (dag)", it.tiltak?.navn)
            assertEquals("AVONOVA HELSE AS", it.tiltak?.arrangoer)
            assertEquals("2022-03-10", it.tiltak?.opprinneligStartdato.toString())
            assertEquals(null, it.tiltak?.opprinneligSluttdato?.toString())
        }
    }

    @Test
    fun `should put brukerregistrert start and sluttdato in soknad`() {
        val soknad = this::class.java.classLoader.getResource("soknad_uten_tiltak_fra_arena.json")!!.readText()
        Soknad.fromJson(soknad).also {
            assertEquals("STERK", it.fornavn)
            assertEquals("LAPP", it.etternavn)
            assertEquals(LocalDate.parse("2022-03-01"), it.brukerRegistrertStartDato)
            assertEquals(LocalDate.parse("2022-03-31"), it.brukerRegistrertSluttDato)
            assertNull(it.systemRegistrertStartDato)
            assertNull(it.systemRegistrertSluttDato)
            assertEquals(false, it.onKvp)
            assertEquals(false, it.onIntroduksjonsprogrammet)
        }
    }

    @Test
    fun `null name in soknad does not throw exception`() {
        val soknad = File("src/test/resources/soknad_barn_med_manglende_navn_pga_kode6.json").readText()
        assertDoesNotThrow { Soknad.fromJson(soknad) }
    }

    @Test
    fun `missing fnr throws exception`() {
        val json = """
            {
              "opprettetDato": "2022-03-11T15:40:03.942Z",
              "fakta": [
                {
                  "key": "personalia",
                  "properties": {}
                  }
                }
              ]
            }
        """.trimIndent()
        assertThrows<IllegalArgumentException> { Soknad.fromJson(json) }
        assertThrows<IllegalArgumentException> { SoknadDetails.fromJson(json) }
    }
}
