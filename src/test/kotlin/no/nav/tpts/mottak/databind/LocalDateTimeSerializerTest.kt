package no.nav.tpts.mottak.databind

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.time.LocalDateTime
import java.time.Month
import kotlin.test.Test
import kotlin.test.assertEquals

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
        // given
        val dateTime = "2022-01-01T14:15:16"
        val json = """{"dateTime":"${dateTime}Z"}"""

        // when
        val classForTest = Json.decodeFromString<ClassForTest>(json)

        // then
        assertEquals(dateTime, classForTest.dateTime.toString())
    }
}
