package no.nav.tiltakspenger.mottak.serder

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class StrictLocalDateSerializerTest {

    @Serializable
    data class ClassForTest(@Serializable(with = StrictLocalDateSerializer::class) val dato: LocalDate)

    @Test
    fun `Deserializes LocalDate`() {
        // given
        val dato = "2022-01-01"
        val json = """{"dato":"$dato"}"""

        // when
        val classForTest = Json.decodeFromString<ClassForTest>(json)

        // then
        assertEquals(dato, classForTest.dato.toString())
    }
}
