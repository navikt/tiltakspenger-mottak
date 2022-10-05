package no.nav.tiltakspenger.mottak

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ConfigurationTest {
    @Test
    fun `selects default topic-name when no system property is present`() {
        assertEquals("joark.local", Configuration.KafkaConfig().joarkTopic)
    }

    @Test
    fun `unleash strategy is enabled for the correct cluster`() {
        val strategy = ByClusterStrategy("foo")

        assertTrue(strategy.isEnabled(mapOf("cluster" to "foo")))
        assertTrue(strategy.isEnabled(mapOf("cluster" to "FOO")))
        assertTrue(strategy.isEnabled(mapOf("cluster" to "foo,bar")))
        assertFalse(strategy.isEnabled(mapOf("cluster" to "bar")))
        assertFalse(strategy.isEnabled(mapOf("cluster" to "")))
        assertFalse(strategy.isEnabled(mapOf("qux" to "foo")))
        assertTrue(strategy.isEnabled(mapOf("qux" to "foo", "cluster" to "foo")))
        assertFalse(strategy.isEnabled(emptyMap()))
    }
}
