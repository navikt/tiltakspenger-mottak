package no.nav.tiltakspenger.mottak.soknad

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate

internal class TiltakTest {
    @Test fun `soknad med tiltak fra arena`() {
        val json = File("src/test/resources/soknad_med_tiltak_fra_arena.json").readText()
        val tiltak = Tiltak.fromJson(json)
        assertNotNull(tiltak)
        assertEquals("Jobbklubb", tiltak?.navn)
        assertEquals("JOBLEARN AS", tiltak?.arrangoer)
        assertEquals("136347592", tiltak?.id)
        assertEquals(LocalDate.parse("2021-12-06"), tiltak?.opprinneligStartdato)
        assertNull(tiltak?.opprinneligSluttdato)
    }

    @Test fun `soknad uten tiltak fra arena`() {
        val json = File("src/test/resources/soknad_uten_tiltak_fra_arena.json").readText()
        val tiltak = Tiltak.fromJson(json)
        assertNull(tiltak)
    }
}
