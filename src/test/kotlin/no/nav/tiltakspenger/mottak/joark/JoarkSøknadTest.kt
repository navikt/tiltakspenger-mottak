package no.nav.tiltakspenger.mottak.joark

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.nav.tiltakspenger.mottak.joark.models.JoarkSøknad
import no.nav.tiltakspenger.mottak.søknad.Barnetillegg
import no.nav.tiltakspenger.mottak.søknad.Søknad
import no.nav.tiltakspenger.mottak.søknad.SøknadDetails
import no.nav.tiltakspenger.mottak.søknad.TrygdOgPensjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.time.LocalDate
import java.time.Month

internal class JoarkSøknadTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `deserialize soknad`() {
        val rawJsonSoknad = javaClass.getResource("/mocksoknad.json")?.readText(Charsets.UTF_8)!!
        json.decodeFromString<JoarkSøknad>(rawJsonSoknad)
    }

    @Test
    fun `should put joark faktum data into soknad object`() {
        val faktums = this::class.java.classLoader.getResource("faktumsSkjermet.json")!!.readText()
        SøknadDetails.fromJson(faktums).also {
            assertEquals("BRÅKETE", it.søknad.fornavn)
            assertEquals("BLYANT", it.søknad.etternavn)
            assertEquals("14038205537", it.søknad.ident)
            assertEquals("2022-03-10T11:03:35.365", it.søknad.opprettet.toString())
            assertEquals("136950219", it.arenaTiltak?.arenaId)
            assertEquals("ARBRRHDAG", it.arenaTiltak?.tiltakskode)
            assertEquals("AVONOVA HELSE AS", it.arenaTiltak?.arrangoer)
            assertEquals("2022-03-10", it.arenaTiltak?.opprinneligStartdato.toString())
            assertEquals(null, it.arenaTiltak?.opprinneligSluttdato?.toString())
        }
    }

    @Test
    fun `should put brukerregistrert start and sluttdato in soknad`() {
        val soknad = this::class.java.classLoader.getResource("soknad_uten_tiltak_fra_arena.json")!!.readText()
        Søknad.fromJson(soknad, "", "").also {
            assertEquals(LocalDate.parse("2022-03-01"), it.brukerregistrertTiltak?.fom)
            assertEquals(LocalDate.parse("2022-03-31"), it.brukerregistrertTiltak?.tom)
            assertNull(it.arenaTiltak)
        }
    }

    @Test
    fun `null name in soknad does not throw exception`() {
        val soknad = File("src/test/resources/soknad_barn_med_manglende_navn_pga_kode6.json").readText()
        assertDoesNotThrow { Søknad.fromJson(soknad, "", "") }
    }

    @Test
    fun `soker som deltar på intro should have true in deltarIntroduksjonsprogrammet field`() {
        val søknad = Søknad.fromJson(File("src/test/resources/soknad_deltar_intro.json").readText(), "", "")
        assertEquals(true, søknad.deltarIntroduksjonsprogrammet)
    }

    @Test
    fun `soker som IKKE deltar på intro should have false in deltarIntroduksjonsprogrammet field`() {
        val soknadJson = this::class.java.classLoader.getResource("soknad_uten_tiltak_fra_arena.json")!!.readText()
        val søknad = Søknad.fromJson(soknadJson, "", "")
        assertEquals(false, søknad.deltarIntroduksjonsprogrammet)
    }

    @Test
    fun `soker som deltar på kvp should have true in deltarKvp field`() {
        val søknad = Søknad.fromJson(File("src/test/resources/soknad_deltar_kvp.json").readText(), "", "")
        assertEquals(true, søknad.deltarKvp)
    }

    @Test
    fun `soker som IKKE deltar på kvp should have false in deltarKvp field`() {
        val soknadJson = this::class.java.classLoader.getResource("soknad_uten_tiltak_fra_arena.json")!!.readText()
        val søknad = Søknad.fromJson(soknadJson, "", "")
        assertEquals(false, søknad.deltarKvp)
    }

    @Test
    fun `soker på insitusjon should have true in oppholdInstitusjon field`() {
        val søknad =
            Søknad.fromJson(File("src/test/resources/soknad_barn_med_manglende_navn_pga_kode6.json").readText(), "", "")
        assertEquals(true, søknad.oppholdInstitusjon)
        assertEquals("annet", søknad.typeInstitusjon)
    }

    @Test
    fun `soker utenfor insitusjon should have false in oppholdInstitusjon field`() {
        val søknad = Søknad.fromJson(File("src/test/resources/soknad_uten_tiltak_fra_arena.json").readText(), "", "")
        assertEquals(false, søknad.oppholdInstitusjon)
        assertNull(søknad.typeInstitusjon)
    }

    @Test
    fun `should get tiltaks-info from soknad with tiltak from Arena`() {
        val søknad = Søknad.fromJson(File("src/test/resources/soknad_med_tiltak_fra_arena.json").readText(), "", "")
        assertEquals("JOBLEARN AS", søknad.arenaTiltak?.arrangoer)
        assertEquals("JOBBK", søknad.arenaTiltak?.tiltakskode)
    }

    @Test
    fun `should get tiltaks-info from soknad with tiltak from user`() {
        val søknad = Søknad.fromJson(File("src/test/resources/soknad_uten_tiltak_fra_arena.json").readText(), "", "")
        assertEquals("Tull og tøys AS", søknad.brukerregistrertTiltak?.arrangoernavn)
        assertEquals("AMO", søknad.brukerregistrertTiltak?.tiltakskode)
    }

    @Test
    fun `barnetillegg med forhåndsregistrert barn`() {
        val søknad = Søknad.fromJson(File("src/test/resources/soknad_med_tiltak_fra_arena.json").readText(), "", "")
        val expectedBarn = Barnetillegg("16081376917", alder = 8, land = "NOR")
        assertEquals(expectedBarn.land, søknad.barnetillegg.first().land)
        assertEquals(expectedBarn.alder, søknad.barnetillegg.first().alder)
        assertEquals(expectedBarn.ident, søknad.barnetillegg.first().ident)
        assertEquals(expectedBarn.fødselsdato, søknad.barnetillegg.first().fødselsdato)
    }

    @Test
    fun `barnetillegg med manuelt registrert barn`() {
        val søknad = Søknad.fromJson(
            File("src/test/resources/søknad_med_manuelt_lagt_til_barn.json").readText(),
            "",
            ""
        )
        val expectedBarn = Barnetillegg(fødselsdato = LocalDate.of(2019, Month.JANUARY, 1), alder = 3, land = "NOR")
        assertEquals(expectedBarn.land, søknad.barnetillegg.first().land)
        assertEquals(expectedBarn.alder, søknad.barnetillegg.first().alder)
        assertEquals(expectedBarn.ident, søknad.barnetillegg.first().ident)
        assertEquals(expectedBarn.fødselsdato, søknad.barnetillegg.first().fødselsdato)
    }

    @Test
    fun `trygd og pensjon kommer med`() {
        val søknad = Søknad.fromJson(File("src/test/resources/søknad_med_trygd_og_pensjon.json").readText(), "", "")
        val expected1 = TrygdOgPensjon(
            utbetaler = "Manchester United",
            prosent = 42,
            fom = LocalDate.of(2021, Month.FEBRUARY, 9),
            tom = LocalDate.of(2022, Month.AUGUST, 2)
        )
        val expected2 = TrygdOgPensjon(
            utbetaler = "Bayern München",
            prosent = 30,
            fom = LocalDate.of(2022, Month.AUGUST, 10)
        )
        assertTrue(søknad.trygdOgPensjon?.size == 2)
        assertTrue(søknad.trygdOgPensjon!!.contains(expected1))
        assertTrue(søknad.trygdOgPensjon!!.contains(expected2))
    }

    @Test
    fun `trygd og pensjon er null om det ikke eksisterer`() {
        val søknad = Søknad.fromJson(File("src/test/resources/mocksoknad.json").readText(), "", "")
        assertNull(søknad.trygdOgPensjon)
    }

    @Test
    fun `fritekst kommer med`() {
        val søknad = Søknad.fromJson(File("src/test/resources/søknad_med_trygd_og_pensjon.json").readText(), "", "")
        assertEquals("en tilleggsopplysning", søknad.fritekst)
    }

    @Test
    fun `søknad med null i fritekst`() {
        val søknad = Søknad.fromJson(File("src/test/resources/soknad_uten_tiltak_fra_arena.json").readText(), "", "")
        assertTrue(søknad.fritekst.isNullOrEmpty())
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
        assertThrows<IllegalArgumentException> { Søknad.fromJson(json, "", "") }
        assertThrows<IllegalArgumentException> { SøknadDetails.fromJson(json) }
    }
}
