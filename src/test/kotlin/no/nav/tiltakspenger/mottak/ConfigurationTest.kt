package no.nav.tiltakspenger.mottak

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ConfigurationTest {
    @Test
    fun `selects default topic-name when no system property is present`() {
        assertEquals("teamdokumenthandtering.aapen-dok-journalfoering-q1", joarkTopicName())
    }

    @Test
    fun `unleash strategy is enabled for the correct cluster`() {
        val strategy = ByClusterStrategy("foo")

        assertTrue(strategy.isEnabled(mapOf("cluster" to "foo")))
        assertFalse(strategy.isEnabled(mapOf("cluster" to "bar")))
    }
}
