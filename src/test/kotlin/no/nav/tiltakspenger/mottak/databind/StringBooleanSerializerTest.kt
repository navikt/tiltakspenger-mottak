package no.nav.tiltakspenger.mottak.databind

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class StringBooleanSerializerTest {

    @Serializable
    data class ClassForTest(@Serializable(with = StringBooleanSerializer::class) val bar: Boolean)

    @Test
    fun deserialize() {
        assertTrue(Json.decodeFromString<ClassForTest>("""{"bar":"true"}""").bar)
        assertFalse(Json.decodeFromString<ClassForTest>("""{"bar":"false"}""").bar)
        assertFalse(Json.decodeFromString<ClassForTest>("""{"bar":"hei"}""").bar)
        assertFalse(Json.decodeFromString<ClassForTest>("""{"bar":null}""").bar)
    }
}
