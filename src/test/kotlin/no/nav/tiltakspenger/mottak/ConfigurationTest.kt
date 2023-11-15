package no.nav.tiltakspenger.mottak

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.net.URISyntaxException

internal class ConfigurationTest {
    @Test
    fun `selects default topic-name when no system property is present`() {
        assertEquals("joark.local", Configuration.KafkaConfig().joarkTopic)
    }

    @Test
    fun `gir ikke feil når URL er gyldig`() {
        assertDoesNotThrow { Configuration.SafConfig(baseUrl = "https://example.org") }
    }

    @Test
    fun `gir feil når URL ikke er gyldig`() {
        assertThrows<URISyntaxException> {
            Configuration.SafConfig(baseUrl = "ikke en url")
        }
    }
}
