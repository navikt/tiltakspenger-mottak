package no.nav.tpts.mottak

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ConfigurationTest {

    @Test
    fun `foo`() {
        assertEquals("teamdokumenthandtering.aapen-dok-journalfoering-q1", topicName())
    }
}
