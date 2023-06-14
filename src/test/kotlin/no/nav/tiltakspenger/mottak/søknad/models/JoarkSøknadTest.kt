package no.nav.tiltakspenger.mottak.søknad.models

import no.nav.tiltakspenger.mottak.søknad.BarnetilleggDTO
import no.nav.tiltakspenger.mottak.søknad.DokumentInfoDTO
import no.nav.tiltakspenger.mottak.søknad.JaNeiSpmDTO
import no.nav.tiltakspenger.mottak.søknad.SpmSvarDTO
import no.nav.tiltakspenger.mottak.søknad.SøknadDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.time.LocalDate

internal class JoarkSøknadTest {
    val dokInfo = DokumentInfoDTO(
        journalpostId = "journalpostId",
        dokumentInfoId = "dokumentInfoId",
        filnavn = "tiltakspenger.json",
    )

    @Test
    fun `from ny søknad som feiler med mangler ident`() {
        val faktums = this::class.java.classLoader.getResource("ny_søknad.json")!!.readText()
        SøknadDTO.fromSøknadV4(faktums, dokInfo).also {
            assertEquals("NØDVENDIG", it.personopplysninger.fornavn)
            assertEquals("HOFTE", it.personopplysninger.etternavn)
            assertEquals("09877698987", it.personopplysninger.ident)
        }
    }

    @Test
    fun `from json to Søknad with ArenaTiltak`() {
        val faktums = this::class.java.classLoader.getResource("søknad_med_tiltak_fra_arena.json")!!.readText()
        SøknadDTO.fromGammelSøknad(faktums, dokInfo).also {
            assertEquals("TALENTFULL", it.personopplysninger.fornavn)
            assertEquals("BOLLE", it.personopplysninger.etternavn)
            assertEquals("20058803546", it.personopplysninger.ident)
            assertEquals("2021-12-20T13:08:38.444", it.opprettet.toString())
            assertEquals("136347592", it.arenaTiltak?.arenaId)
            assertEquals("JOBBK", it.arenaTiltak?.tiltakskode)
            assertEquals("JOBLEARN AS", it.arenaTiltak?.arrangoernavn)
            assertEquals("2021-12-06", it.arenaTiltak?.opprinneligStartdato.toString())
            assertEquals(null, it.arenaTiltak?.opprinneligSluttdato?.toString())
        }
    }

    @Test
    fun `should put brukerregistrert start and sluttdato in soknad`() {
        val soknad = this::class.java.classLoader.getResource("søknad_uten_tiltak_fra_arena.json")!!.readText()
        SøknadDTO.fromGammelSøknad(soknad, dokInfo).also {
            assertEquals(LocalDate.parse("2022-03-01"), it.brukerTiltak?.fom)
            assertEquals(LocalDate.parse("2022-03-31"), it.brukerTiltak?.tom)
            assertNull(it.arenaTiltak)
        }
    }

    @Test
    fun `null name in soknad does not throw exception`() {
        val soknad = File("src/test/resources/søknad_barn_med_manglende_navn_pga_kode6.json").readText()
        assertDoesNotThrow { SøknadDTO.fromGammelSøknad(soknad, dokInfo) }
    }

    @Test
    fun `soker som deltar på intro should have true in deltarIntroduksjonsprogrammet field`() {
        val søknad = SøknadDTO.fromGammelSøknad(File("src/test/resources/søknad_deltar_intro.json").readText(), dokInfo)
        assertTrue(søknad.intro.svar == SpmSvarDTO.Ja)
    }

    @Test
    fun `soker som deltar på intro should have detaljer`() {
        val søknad = SøknadDTO.fromGammelSøknad(File("src/test/resources/søknad_deltar_intro.json").readText(), dokInfo)
        assertTrue(søknad.intro.svar == SpmSvarDTO.Ja)
        assertEquals(LocalDate.of(2022, 4, 1), søknad.intro.fom)
        assertEquals(LocalDate.of(2022, 4, 30), søknad.intro.tom)
    }

    @Test
    fun `soker som ikke får spørsmål om introduksjonsprogrammet vet vi ingenting om`() {
        val soknadJson = this::class.java.classLoader.getResource("søknad_uten_tiltak_fra_arena.json")!!.readText()
        val søknad = SøknadDTO.fromGammelSøknad(soknadJson, dokInfo)
        assertTrue(søknad.intro.svar == SpmSvarDTO.IkkeBesvart)
        assertNull(søknad.intro.fom)
        assertNull(søknad.intro.tom)
    }

    @Test
    fun `soker som IKKE deltar på intro should have false in deltarIntroduksjonsprogrammet field`() {
        val soknadJson = this::class.java.classLoader.getResource("søknad_deltar_ikke_intro.json")!!.readText()
        val søknad = SøknadDTO.fromGammelSøknad(soknadJson, dokInfo)
        assertTrue(søknad.intro.svar == SpmSvarDTO.Nei)
        assertNull(søknad.intro.fom)
        assertNull(søknad.intro.tom)
    }

    @Test
    fun `soker som deltar på kvp should have true in deltarKvp field`() {
        val soknadJson = this::class.java.classLoader.getResource("søknad_deltar_kvp.json")!!.readText()
        val søknad = SøknadDTO.fromGammelSøknad(soknadJson, dokInfo)
        assertTrue(søknad.kvp.svar == SpmSvarDTO.Ja)
    }

    @Test
    fun `soker som IKKE deltar på kvp should have false in deltarKvp field`() {
        val soknadJson = this::class.java.classLoader.getResource("søknad_uten_tiltak_fra_arena.json")!!.readText()
        val søknad = SøknadDTO.fromGammelSøknad(soknadJson, dokInfo)
        assertTrue(søknad.kvp.svar == SpmSvarDTO.Nei)
    }

    @Test
    fun `barn med dnr blir mappet riktig`() {
        val soknadJson = this::class.java.classLoader.getResource("gammel_søknad_med_barn_som_har_dnummer.json")!!.readText()
        val søknad = SøknadDTO.fromGammelSøknad(soknadJson, dokInfo)
        assertTrue(søknad.barnetilleggPdl[0].fødselsdato == LocalDate.of(2018, 11, 1))
        assertTrue(søknad.barnetilleggPdl[1].fødselsdato == LocalDate.of(2010, 1, 13))
    }

    @Test
    fun `soker på insitusjon should have true in oppholdInstitusjon field`() {
        val soknadJson = this::class.java.classLoader.getResource("søknad_barn_med_manglende_navn_pga_kode6.json")!!.readText()
        val søknad = SøknadDTO.fromGammelSøknad(soknadJson, dokInfo)
        assertTrue(søknad.institusjon.svar == SpmSvarDTO.Ja)
    }

    @Test
    fun `soker utenfor insitusjon should have false in oppholdInstitusjon field`() {
        val soknadJson = this::class.java.classLoader.getResource("søknad_uten_tiltak_fra_arena.json")!!.readText()
        val søknad = SøknadDTO.fromGammelSøknad(soknadJson, dokInfo)
        assertTrue(søknad.institusjon.svar == SpmSvarDTO.Nei)
    }

    @Test
    fun `should get tiltaks-info from soknad with tiltak from Arena`() {
        val soknadJson = this::class.java.classLoader.getResource("søknad_med_tiltak_fra_arena.json")!!.readText()
        val søknad = SøknadDTO.fromGammelSøknad(soknadJson, dokInfo)

        assertEquals("JOBLEARN AS", søknad.arenaTiltak?.arrangoernavn)
        assertEquals("JOBBK", søknad.arenaTiltak?.tiltakskode)
    }

    @Test
    fun `should get tiltaks-info from soknad with tiltak from user`() {
        val soknadJson = this::class.java.classLoader.getResource("søknad_uten_tiltak_fra_arena.json")!!.readText()
        val søknad = SøknadDTO.fromGammelSøknad(soknadJson, dokInfo)

        assertEquals("Tull og tøys AS", søknad.brukerTiltak?.arrangoernavn)
        assertEquals("AMO", søknad.brukerTiltak?.tiltakskode)
    }

    @Test
    fun `barnetillegg med forhåndsregistrert barn`() {
        val soknadJson = this::class.java.classLoader.getResource("søknad_med_tiltak_fra_arena.json")!!.readText()
        val søknad = SøknadDTO.fromGammelSøknad(soknadJson, dokInfo)

        val expectedBarn = BarnetilleggDTO(
            oppholderSegIEØS = JaNeiSpmDTO(svar = SpmSvarDTO.Ja),
            fornavn = "KORRUPT",
            mellomnavn = null,
            etternavn = "STAUDE",
            fødselsdato = LocalDate.of(2015, 1, 26),
        )
        assertEquals(expectedBarn.fornavn, søknad.barnetilleggPdl.first().fornavn)
        assertEquals(expectedBarn.mellomnavn, søknad.barnetilleggPdl.first().mellomnavn)
        assertEquals(expectedBarn.etternavn, søknad.barnetilleggPdl.first().etternavn)
        assertEquals(expectedBarn.oppholderSegIEØS, søknad.barnetilleggPdl.first().oppholderSegIEØS)
        assertEquals(expectedBarn.fødselsdato, søknad.barnetilleggPdl.first().fødselsdato)
    }

    @Test
    fun `barnetillegg med mellomnavn i barn`() {
        val soknadJson = this::class.java.classLoader.getResource("søknad_med_tiltak_fra_arena_null_sluttdato.json")!!.readText()
        val søknad = SøknadDTO.fromGammelSøknad(soknadJson, dokInfo)

        val expectedBarn = BarnetilleggDTO(
            oppholderSegIEØS = JaNeiSpmDTO(svar = SpmSvarDTO.Ja),
            fornavn = "foo",
            mellomnavn = "foo",
            etternavn = "bar",
            fødselsdato = LocalDate.of(2015, 3, 12),
        )
        assertEquals(expectedBarn.fornavn, søknad.barnetilleggPdl.first().fornavn)
        assertEquals(expectedBarn.mellomnavn, søknad.barnetilleggPdl.first().mellomnavn)
        assertEquals(expectedBarn.etternavn, søknad.barnetilleggPdl.first().etternavn)
        assertEquals(expectedBarn.oppholderSegIEØS, søknad.barnetilleggPdl.first().oppholderSegIEØS)
        assertEquals(expectedBarn.fødselsdato, søknad.barnetilleggPdl.first().fødselsdato)
    }

    @Test
    fun `barnetillegg med manuelt registrert barn`() {
        val soknadJson = this::class.java.classLoader.getResource("søknad_med_manuelt_lagt_til_barn.json")!!.readText()
        val søknad = SøknadDTO.fromGammelSøknad(soknadJson, dokInfo)

        val expectedBarn = BarnetilleggDTO(
            fødselsdato = LocalDate.of(2019, 1, 1),
            fornavn = "Kjell T",
            mellomnavn = null,
            etternavn = "Ring",
            oppholderSegIEØS = JaNeiSpmDTO(svar = SpmSvarDTO.Ja),
        )
        assertEquals(expectedBarn.oppholderSegIEØS, søknad.barnetilleggManuelle.first().oppholderSegIEØS)
        assertEquals(expectedBarn.fødselsdato, søknad.barnetilleggManuelle.first().fødselsdato)
        assertEquals(expectedBarn.fornavn, søknad.barnetilleggManuelle.first().fornavn)
        assertEquals(expectedBarn.mellomnavn, søknad.barnetilleggManuelle.first().mellomnavn)
        assertEquals(expectedBarn.etternavn, søknad.barnetilleggManuelle.first().etternavn)
    }

    @Test
    fun `trygd og pensjon kommer med`() {
        val soknadJson = this::class.java.classLoader.getResource("søknad_med_trygd_og_pensjon.json")!!.readText()
        val søknad = SøknadDTO.fromGammelSøknad(soknadJson, dokInfo)

        val expected = JaNeiSpmDTO(
            svar = SpmSvarDTO.Ja,
        )
        assertEquals(expected, søknad.etterlønn)
    }

    @Test
    fun `trygd og pensjon er empty om det ikke eksisterer`() {
        val soknadJson = this::class.java.classLoader.getResource("søknad.json")!!.readText()
        val søknad = SøknadDTO.fromGammelSøknad(soknadJson, dokInfo)

        val expected = JaNeiSpmDTO(
            svar = SpmSvarDTO.Nei,
        )
        assertEquals(expected, søknad.etterlønn)
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
        assertThrows<IllegalArgumentException> { SøknadDTO.fromGammelSøknad(json, dokInfo) }
    }
}
