package no.nav.tiltakspenger.mottak.joark

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.nav.tiltakspenger.mottak.joark.models.JoarkSoknad
import no.nav.tiltakspenger.mottak.søknad.SøknadDetails
import no.nav.tiltakspenger.mottak.søknad.søknadList.Barnetillegg
import no.nav.tiltakspenger.mottak.søknad.søknadList.Søknad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.time.LocalDate

internal class JoarkSøknadTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `deserialize soknad`() {
        val rawJsonSoknad = javaClass.getResource("/mocksoknad.json")?.readText(Charsets.UTF_8)!!
        json.decodeFromString<JoarkSoknad>(rawJsonSoknad)
    }

    @Test
    fun `should put joark faktum data into soknad object`() {
        val faktums = this::class.java.classLoader.getResource("faktumsSkjermet.json")!!.readText()
        SøknadDetails.fromJson(faktums).also {
            assertEquals("BRÅKETE", it.søknad.fornavn)
            assertEquals("BLYANT", it.søknad.etternavn)
            assertEquals("14038205537", it.søknad.ident)
            assertEquals("2022-03-10T11:03:35.365", it.søknad.opprettet.toString())
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
        Søknad.fromJson(soknad).also {
            assertEquals(LocalDate.parse("2022-03-01"), it.brukerRegistrertStartDato)
            assertEquals(LocalDate.parse("2022-03-31"), it.brukerRegistrertSluttDato)
            assertNull(it.systemRegistrertStartDato)
            assertNull(it.systemRegistrertSluttDato)
        }
    }

    @Test
    fun `null name in soknad does not throw exception`() {
        val soknad = File("src/test/resources/soknad_barn_med_manglende_navn_pga_kode6.json").readText()
        assertDoesNotThrow { Søknad.fromJson(soknad) }
    }

    @Test
    fun `soker som deltar på intro should have true in deltarIntroduksjonsprogrammet field`() {
        val søknad = Søknad.fromJson(File("src/test/resources/soknad_deltar_intro.json").readText())
        assertEquals(true, søknad.deltarIntroduksjonsprogrammet)
    }

    @Test
    fun `soker som IKKE deltar på intro should have false in deltarIntroduksjonsprogrammet field`() {
        val soknadJson = this::class.java.classLoader.getResource("soknad_uten_tiltak_fra_arena.json")!!.readText()
        val søknad = Søknad.fromJson(soknadJson)
        assertEquals(false, søknad.deltarIntroduksjonsprogrammet)
    }

    @Test
    fun `soker som deltar på kvp should have true in deltarKvp field`() {
        val søknad = Søknad.fromJson(File("src/test/resources/soknad_deltar_kvp.json").readText())
        assertEquals(true, søknad.deltarKvp)
    }

    @Test
    fun `soker som IKKE deltar på kvp should have false in deltarKvp field`() {
        val soknadJson = this::class.java.classLoader.getResource("soknad_uten_tiltak_fra_arena.json")!!.readText()
        val søknad = Søknad.fromJson(soknadJson)
        assertEquals(false, søknad.deltarKvp)
    }

    @Test
    fun `soker på insitusjon should have true in oppholdInstitusjon field`() {
        val søknad =
            Søknad.fromJson(File("src/test/resources/soknad_barn_med_manglende_navn_pga_kode6.json").readText())
        assertEquals(true, søknad.oppholdInstitusjon)
        assertEquals("annet", søknad.typeInstitusjon)
    }

    @Test
    fun `soker utenfor insitusjon should have false in oppholdInstitusjon field`() {
        val søknad = Søknad.fromJson(File("src/test/resources/soknad_uten_tiltak_fra_arena.json").readText())
        assertEquals(false, søknad.oppholdInstitusjon)
        assertNull(søknad.typeInstitusjon)
    }

    @Test
    fun `should get tiltaks-info from soknad with tiltak from Arena`() {
        val søknad = Søknad.fromJson(File("src/test/resources/soknad_med_tiltak_fra_arena.json").readText())
        assertEquals("JOBLEARN AS", søknad.tiltaksArrangoer)
        assertEquals("Jobbklubb", søknad.tiltaksType)
    }

    @Test
    fun `should get tiltaks-info from soknad with tiltak from user`() {
        val søknad = Søknad.fromJson(File("src/test/resources/soknad_uten_tiltak_fra_arena.json").readText())
        assertEquals("Tull og tøys AS", søknad.tiltaksArrangoer)
        assertEquals("AMO", søknad.tiltaksType)
    }

    @Test
    fun `should barnetillegg from soknad`() {
        val søknad = Søknad.fromJson(File("src/test/resources/soknad_med_tiltak_fra_arena.json").readText())
        val expectedBarn = Barnetillegg("SMEKKER", "STAUDE", 8, "16081376917", "NOR")
        assertEquals(expectedBarn.fornavn, søknad.barnetillegg.first().fornavn)
        assertEquals(expectedBarn.etternavn, søknad.barnetillegg.first().etternavn)
        assertEquals(expectedBarn.bosted, søknad.barnetillegg.first().bosted)
        assertEquals(expectedBarn.alder, søknad.barnetillegg.first().alder)
        assertEquals(expectedBarn.ident, søknad.barnetillegg.first().ident)
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
        assertThrows<IllegalArgumentException> { Søknad.fromJson(json) }
        assertThrows<IllegalArgumentException> { SøknadDetails.fromJson(json) }
    }
}
