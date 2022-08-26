package no.nav.tiltakspenger.mottak.søknad

import no.nav.tiltakspenger.mottak.joark.models.Faktum
import no.nav.tiltakspenger.mottak.joark.models.JoarkSoknad
import no.nav.tiltakspenger.mottak.joark.models.Properties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

internal class BrukerregistrertTiltakTest {
    @Test
    fun `happy day`() {
        val fraOgMed = LocalDate.of(2022, Month.JANUARY, 22)
        val tilOgMed = LocalDate.of(2022, Month.FEBRUARY, 11)
        val properties = Properties(
            arrangoernavn = "arrangørnavn",
            fom = fraOgMed,
            tom = tilOgMed,
            adresse = "en gate",
            postnummer = "et postnummer",
            antallDager = "12 dager",
        )
        val faktum = Faktum(key = "tiltaksliste.annetTiltak", value = "AMO", properties = properties)
        val joarkSoknad = JoarkSoknad(fakta = listOf(faktum), opprettetDato = LocalDateTime.MIN)

        val tiltak = BrukerregistrertTiltak.fromJoarkSoknad(joarkSoknad)

        assertNotNull(tiltak)
        assertEquals("AMO", tiltak?.tiltakstype)
//        assertEquals("beskrivelse", tiltak?.beskrivelse)
        assertEquals(fraOgMed, tiltak?.fom)
        assertEquals(tilOgMed, tiltak?.tom)
        assertEquals("en gate", tiltak?.adresse)
        assertEquals("et postnummer", tiltak?.postnummer)
        assertEquals("arrangørnavn", tiltak?.arrangoernavn)
        assertEquals(12, tiltak?.antallDager)
    }

    @Test
    fun `tiltak is null when key is not found`() {
        val faktum = Faktum(key = "en faktumnøkkel vi ikke har noe forhold til")
        val joarkSoknad = JoarkSoknad(fakta = listOf(faktum), opprettetDato = LocalDateTime.MIN)

        val tiltak = BrukerregistrertTiltak.fromJoarkSoknad(joarkSoknad)

        assertNull(tiltak)
    }
}
