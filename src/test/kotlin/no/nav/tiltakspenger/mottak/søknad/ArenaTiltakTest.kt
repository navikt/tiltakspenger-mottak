package no.nav.tiltakspenger.mottak.søknad

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate

internal class ArenaTiltakTest {
    @Test
    fun `soknad med tiltak fra arena`() {
        val json = File("src/test/resources/søknad_med_tiltak_fra_arena.json").readText()
        val arenaTiltak = ArenaTiltak.fromJson(json)
        assertNotNull(arenaTiltak)
        assertEquals("JOBBK", arenaTiltak?.tiltakskode)
        assertEquals("JOBLEARN AS", arenaTiltak?.arrangoer)
        assertEquals("136347592", arenaTiltak?.arenaId)
        assertEquals(LocalDate.parse("2021-12-06"), arenaTiltak?.opprinneligStartdato)
        assertNull(arenaTiltak?.opprinneligSluttdato)
    }

    @Test
    fun `soknad med tiltak fra arena som mangler arrangørnavn`() {
        val json = File("src/test/resources/søknad_med_tiltak_fra_arena_uten_sluttdato_og_arrangør.json").readText()
        val arenaTiltak = ArenaTiltak.fromJson(json)
        assertNotNull(arenaTiltak)
        assertEquals("UTVAOONAV", arenaTiltak?.tiltakskode)
        assertNull(arenaTiltak?.arrangoer)
        assertEquals("138377366", arenaTiltak?.arenaId)
        assertEquals(LocalDate.parse("2022-09-01"), arenaTiltak?.opprinneligStartdato)
        assertNull(arenaTiltak?.opprinneligSluttdato)
    }

    @Test
    fun `soknad uten tiltak fra arena`() {
        val json = File("src/test/resources/søknad_uten_tiltak_fra_arena.json").readText()
        val arenaTiltak = ArenaTiltak.fromJson(json)
        assertNull(arenaTiltak)
    }
}
