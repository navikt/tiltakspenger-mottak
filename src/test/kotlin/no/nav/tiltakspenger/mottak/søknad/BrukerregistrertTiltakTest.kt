package no.nav.tiltakspenger.mottak.søknad

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.nav.tiltakspenger.mottak.joark.models.Faktum
import no.nav.tiltakspenger.mottak.joark.models.JoarkSoknad
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
        val jsonFromFile = File("src/test/resources/soknad_uten_tiltak_fra_arena.json").readText()
        val joarkSoknad = json.decodeFromString<JoarkSoknad>(jsonFromFile)

        val tiltak = BrukerregistrertTiltak.fromJoarkSoknad(joarkSoknad)

        assertNotNull(tiltak)
        assertEquals("AMO", tiltak?.tiltakstype)
//        assertEquals("beskrivelse", tiltak?.beskrivelse)
        assertEquals(LocalDate.of(2022, Month.MARCH, 1), tiltak?.fom)
        assertEquals(LocalDate.of(2022, Month.MARCH, 31), tiltak?.tom)
        assertEquals("Storgata 1", tiltak?.adresse)
        assertEquals("0318", tiltak?.postnummer)
        assertEquals("Tull og tøys AS", tiltak?.arrangoernavn)
        assertEquals(5, tiltak?.antallDager)
    }

    @Test
    fun `søknad uten brukerregistrert tiltak`() {
        val faktum = Faktum(key = "en faktumnøkkel vi ikke har noe forhold til")
        val joarkSoknad = JoarkSoknad(fakta = listOf(faktum), opprettetDato = LocalDateTime.MIN)

        val tiltak = BrukerregistrertTiltak.fromJoarkSoknad(joarkSoknad)

        assertNull(tiltak)
    }
}
