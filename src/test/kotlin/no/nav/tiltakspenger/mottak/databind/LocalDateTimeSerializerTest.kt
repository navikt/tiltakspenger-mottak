package no.nav.tiltakspenger.mottak.databind

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.time.LocalDateTime
import java.time.Month

internal class LocalDateTimeSerializerTest {
    @Serializable
    data class ClassForTest(@Serializable(with = LocalDateTimeSerializer::class) val dateTime: LocalDateTime)

    @Test
    fun `Serializes LocalDateTime`() {
        // given
        val dateTime = LocalDateTime.of(2022, Month.JANUARY, 1, 14, 15, 16)
        val classForTest = ClassForTest(dateTime)

        // when
        val json = Json.encodeToString(classForTest)

        // then
        JSONAssert.assertEquals("{dateTime:'2022-01-01T14:15:16'}", json, JSONCompareMode.LENIENT)
    }

    @Test
    fun `Deserializes LocalDateTime`() {
        assertEquals(
            LocalDateTime.of(2022, Month.MARCH, 25, 14, 41, 7),
            Json.decodeFromString<ClassForTest>("""{"dateTime":"2022-03-25T13:41:07.000Z"}""").dateTime
        )

        assertEquals(
            LocalDateTime.of(2022, Month.MARCH, 25, 14, 41, 7),
            Json.decodeFromString<ClassForTest>("""{"dateTime":"2022-03-25T14:41:07.000+01:00"}""").dateTime
        )
    }
}
