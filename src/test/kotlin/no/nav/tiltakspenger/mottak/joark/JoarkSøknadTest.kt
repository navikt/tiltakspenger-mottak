package no.nav.tiltakspenger.mottak.joark

import no.nav.tiltakspenger.mottak.søknad.Barnetillegg
import no.nav.tiltakspenger.mottak.søknad.Søknad
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

    @Test
    fun `from json to Søknad with ArenaTiltak`() {
        val faktums = this::class.java.classLoader.getResource("søknad_med_tiltak_fra_arena.json")!!.readText()
        Søknad.fromJson(faktums, "42", "43").also {
            assertEquals("TALENTFULL", it.fornavn)
            assertEquals("BOLLE", it.etternavn)
            assertEquals("20058803546", it.ident)
            assertEquals("2021-12-20T13:08:38.444", it.opprettet.toString())
            assertEquals("136347592", it.arenaTiltak?.arenaId)
            assertEquals("JOBBK", it.arenaTiltak?.tiltakskode)
            assertEquals("JOBLEARN AS", it.arenaTiltak?.arrangoer)
            assertEquals("2021-12-06", it.arenaTiltak?.opprinneligStartdato.toString())
            assertEquals(null, it.arenaTiltak?.opprinneligSluttdato?.toString())
        }
    }

    @Test
    fun `should put brukerregistrert start and sluttdato in soknad`() {
        val soknad = this::class.java.classLoader.getResource("søknad_uten_tiltak_fra_arena.json")!!.readText()
        Søknad.fromJson(soknad, "", "").also {
            assertEquals(LocalDate.parse("2022-03-01"), it.brukerregistrertTiltak?.fom)
            assertEquals(LocalDate.parse("2022-03-31"), it.brukerregistrertTiltak?.tom)
            assertNull(it.arenaTiltak)
        }
    }

    @Test
    fun `null name in soknad does not throw exception`() {
        val soknad = File("src/test/resources/søknad_barn_med_manglende_navn_pga_kode6.json").readText()
        assertDoesNotThrow { Søknad.fromJson(soknad, "", "") }
    }

    @Test
    fun `soker som deltar på intro should have true in deltarIntroduksjonsprogrammet field`() {
        val søknad = Søknad.fromJson(File("src/test/resources/søknad_deltar_intro.json").readText(), "", "")
        assertEquals(true, søknad.deltarIntroduksjonsprogrammet)
    }

    @Test
    fun `soker som deltar på intro should have detaljer`() {
        val søknad = Søknad.fromJson(File("src/test/resources/søknad_deltar_intro.json").readText(), "", "")
        assertEquals(LocalDate.of(2022, 4, 1), søknad.introduksjonsprogrammetDetaljer?.fom)
        assertEquals(LocalDate.of(2022, 4, 30), søknad.introduksjonsprogrammetDetaljer?.tom)
    }

    @Test
    fun `soker som IKKE deltar på intro should have false in deltarIntroduksjonsprogrammet field`() {
        val soknadJson = this::class.java.classLoader.getResource("søknad_uten_tiltak_fra_arena.json")!!.readText()
        val søknad = Søknad.fromJson(soknadJson, "", "")
        assertEquals(false, søknad.deltarIntroduksjonsprogrammet)
        assertNull(søknad.introduksjonsprogrammetDetaljer?.fom)
        assertNull(søknad.introduksjonsprogrammetDetaljer?.tom)
    }

    @Test
    fun `soker som deltar på kvp should have true in deltarKvp field`() {
        val søknad = Søknad.fromJson(File("src/test/resources/søknad_deltar_kvp.json").readText(), "", "")
        assertEquals(true, søknad.deltarKvp)
    }

    @Test
    fun `soker som IKKE deltar på kvp should have false in deltarKvp field`() {
        val soknadJson = this::class.java.classLoader.getResource("søknad_uten_tiltak_fra_arena.json")!!.readText()
        val søknad = Søknad.fromJson(soknadJson, "", "")
        assertEquals(false, søknad.deltarKvp)
    }

    @Test
    fun `soker på insitusjon should have true in oppholdInstitusjon field`() {
        val søknad =
            Søknad.fromJson(File("src/test/resources/søknad_barn_med_manglende_navn_pga_kode6.json").readText(), "", "")
        assertEquals(true, søknad.oppholdInstitusjon)
        assertEquals("annet", søknad.typeInstitusjon)
    }

    @Test
    fun `soker utenfor insitusjon should have false in oppholdInstitusjon field`() {
        val søknad = Søknad.fromJson(File("src/test/resources/søknad_uten_tiltak_fra_arena.json").readText(), "", "")
        assertEquals(false, søknad.oppholdInstitusjon)
        assertNull(søknad.typeInstitusjon)
    }

    @Test
    fun `should get tiltaks-info from soknad with tiltak from Arena`() {
        val søknad = Søknad.fromJson(File("src/test/resources/søknad_med_tiltak_fra_arena.json").readText(), "", "")
        assertEquals("JOBLEARN AS", søknad.arenaTiltak?.arrangoer)
        assertEquals("JOBBK", søknad.arenaTiltak?.tiltakskode)
    }

    @Test
    fun `should get tiltaks-info from soknad with tiltak from user`() {
        val søknad = Søknad.fromJson(File("src/test/resources/søknad_uten_tiltak_fra_arena.json").readText(), "", "")
        assertEquals("Tull og tøys AS", søknad.brukerregistrertTiltak?.arrangoernavn)
        assertEquals("AMO", søknad.brukerregistrertTiltak?.tiltakskode)
    }

    @Test
    fun `barnetillegg med forhåndsregistrert barn`() {
        val søknad = Søknad.fromJson(File("src/test/resources/søknad_med_tiltak_fra_arena.json").readText(), "", "")
        val expectedBarn = Barnetillegg(
            "26011579360",
            fornavn = "KORRUPT",
            etternavn = "STAUDE",
            alder = 6,
            land = "NOR",
            søktBarnetillegg = false
        )
        assertEquals(expectedBarn.fornavn, søknad.barnetillegg.first().fornavn)
        assertEquals(expectedBarn.etternavn, søknad.barnetillegg.first().etternavn)
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
        val expectedBarn = Barnetillegg(
            fødselsdato = LocalDate.of(2019, Month.JANUARY, 1),
            alder = 3,
            land = "NOR",
            søktBarnetillegg = true
        )
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
            fom = null,
            tom = null
        )
        assertTrue(søknad.trygdOgPensjon.size == 2)
        assertTrue(søknad.trygdOgPensjon.contains(expected1))
        assertTrue(søknad.trygdOgPensjon.contains(expected2))
    }

    @Test
    fun `trygd og pensjon er empty om det ikke eksisterer`() {
        val søknad = Søknad.fromJson(File("src/test/resources/søknad.json").readText(), "", "")
        assertTrue(søknad.trygdOgPensjon.isEmpty())
    }

    @Test
    fun `fritekst kommer med`() {
        val søknad = Søknad.fromJson(File("src/test/resources/søknad_med_trygd_og_pensjon.json").readText(), "", "")
        assertEquals("en tilleggsopplysning", søknad.fritekst)
    }

    @Test
    fun `søknad med null i fritekst`() {
        val søknad = Søknad.fromJson(File("src/test/resources/søknad_uten_tiltak_fra_arena.json").readText(), "", "")
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
    }
}
