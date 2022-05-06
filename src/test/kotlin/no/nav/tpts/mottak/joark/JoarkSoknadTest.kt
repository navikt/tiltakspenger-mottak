package no.nav.tpts.mottak.joark

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.nav.tpts.mottak.joark.models.JoarkSoknad
import no.nav.tpts.mottak.soknad.SoknadDetails
import no.nav.tpts.mottak.soknad.soknadList.Barnetillegg
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
            assertEquals("2022-03-10T11:03:35.365", it.soknad.opprettet.toString())
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
            assertEquals(LocalDate.parse("2022-03-01"), it.brukerRegistrertStartDato)
            assertEquals(LocalDate.parse("2022-03-31"), it.brukerRegistrertSluttDato)
            assertNull(it.systemRegistrertStartDato)
            assertNull(it.systemRegistrertSluttDato)
        }
    }

    @Test
    fun `null name in soknad does not throw exception`() {
        val soknad = File("src/test/resources/soknad_barn_med_manglende_navn_pga_kode6.json").readText()
        assertDoesNotThrow { Soknad.fromJson(soknad) }
    }

    @Test
    fun `soker som deltar på intro should have true in deltarIntroduksjonsprogrammet field`() {
        val soknad = Soknad.fromJson(File("src/test/resources/soknad_deltar_intro.json").readText())
        assertEquals(true, soknad.deltarIntroduksjonsprogrammet)
    }

    @Test
    fun `soker som IKKE deltar på intro should have false in deltarIntroduksjonsprogrammet field`() {
        val soknadJson = this::class.java.classLoader.getResource("soknad_uten_tiltak_fra_arena.json")!!.readText()
        val soknad = Soknad.fromJson(soknadJson)
        assertEquals(false, soknad.deltarIntroduksjonsprogrammet)
    }

    @Test
    fun `soker som deltar på kvp should have true in deltarKvp field`() {
        val soknad = Soknad.fromJson(File("src/test/resources/soknad_deltar_kvp.json").readText())
        assertEquals(true, soknad.deltarKvp)
    }

    @Test
    fun `soker som IKKE deltar på kvp should have false in deltarKvp field`() {
        val soknadJson = this::class.java.classLoader.getResource("soknad_uten_tiltak_fra_arena.json")!!.readText()
        val soknad = Soknad.fromJson(soknadJson)
        assertEquals(false, soknad.deltarKvp)
    }

    @Test
    fun `soker på insitusjon should have true in oppholdInstitusjon field`() {
        val soknad =
            Soknad.fromJson(File("src/test/resources/soknad_barn_med_manglende_navn_pga_kode6.json").readText())
        assertEquals(true, soknad.oppholdInstitusjon)
        assertEquals("annet", soknad.typeInstitusjon)
    }

    @Test
    fun `soker utenfor insitusjon should have false in oppholdInstitusjon field`() {
        val soknad = Soknad.fromJson(File("src/test/resources/soknad_uten_tiltak_fra_arena.json").readText())
        assertEquals(false, soknad.oppholdInstitusjon)
        assertNull(soknad.typeInstitusjon)
    }

    @Test
    fun `should get tiltaks-info from soknad with tiltak from Arena`() {
        val soknad = Soknad.fromJson(File("src/test/resources/soknad_med_tiltak_fra_arena.json").readText())
        assertEquals("JOBLEARN AS", soknad.tiltaksArrangoer)
        assertEquals("Jobbklubb", soknad.tiltaksType)
    }

    @Test
    fun `should get tiltaks-info from soknad with tiltak from user`() {
        val soknad = Soknad.fromJson(File("src/test/resources/soknad_uten_tiltak_fra_arena.json").readText())
        assertEquals("Tull og tøys AS", soknad.tiltaksArrangoer)
        assertEquals("AMO", soknad.tiltaksType)
    }

    @Test
    fun `should barnetillegg from soknad`() {
        val soknad = Soknad.fromJson(File("src/test/resources/soknad_med_tiltak_fra_arena.json").readText())
        val expectedBarn = Barnetillegg("SMEKKER", "STAUDE", 8, "16081376917", "NOR")
        assertEquals(expectedBarn.fornavn, soknad.barnetillegg.first().fornavn)
        assertEquals(expectedBarn.etternavn, soknad.barnetillegg.first().etternavn)
        assertEquals(expectedBarn.bosted, soknad.barnetillegg.first().bosted)
        assertEquals(expectedBarn.alder, soknad.barnetillegg.first().alder)
        assertEquals(expectedBarn.ident, soknad.barnetillegg.first().ident)
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
