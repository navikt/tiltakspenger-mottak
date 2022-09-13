package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.nav.tiltakspenger.mottak.joark.models.Faktum
import no.nav.tiltakspenger.mottak.joark.models.JoarkSøknad
import no.nav.tiltakspenger.mottak.joark.models.Properties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

internal class BrukerregistrertTiltakTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `søknad med brukerregistrert tiltak`() {
        val jsonFromFile = File("src/test/resources/soknad_deltar_intro.json").readText()
        val joarkSøknad = json.decodeFromString<JoarkSøknad>(jsonFromFile)

        val tiltak = BrukerregistrertTiltak.fromJoarkSoknad(joarkSøknad)

        assertNotNull(tiltak)
        assertEquals("Annet", tiltak?.tiltakskode)
        assertEquals("Intro", tiltak?.beskrivelse)
        assertEquals(LocalDate.of(2022, Month.APRIL, 1), tiltak?.fom)
        assertEquals(LocalDate.of(2022, Month.APRIL, 22), tiltak?.tom)
        assertEquals("Storgata 1", tiltak?.adresse)
        assertEquals("0318", tiltak?.postnummer)
        assertEquals("test as", tiltak?.arrangoernavn)
        assertEquals(5, tiltak?.antallDager)
    }

    @Test
    fun `søknad uten brukerregistrert tiltak`() {
        val faktum = Faktum(key = "en faktumnøkkel vi ikke har noe forhold til")
        val joarkSøknad = JoarkSøknad(fakta = listOf(faktum), opprettetDato = LocalDateTime.MIN)

        val tiltak = BrukerregistrertTiltak.fromJoarkSoknad(joarkSøknad)

        assertNull(tiltak)
    }

    @Test
    fun `ingen dager angitt gir 0 dager`() {
        val faktum = Faktum(key = "tiltaksliste.annetTiltak", properties = Properties(antallDager = null))
        val joarkSøknad = JoarkSøknad(fakta = listOf(faktum), opprettetDato = LocalDateTime.MIN)

        val tiltak = BrukerregistrertTiltak.fromJoarkSoknad(joarkSøknad)

        assertEquals(0, tiltak?.antallDager)
    }
}
