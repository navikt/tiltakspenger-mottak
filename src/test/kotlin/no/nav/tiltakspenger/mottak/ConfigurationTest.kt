package no.nav.tiltakspenger.mottak

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ConfigurationTest {
    @Test
    fun `selects default topic-name when no system property is present`() {
        assertEquals("teamdokumenthandtering.aapen-dok-journalfoering-q1", topicName())
    }
}
