package no.nav.tiltakspenger.mottak.databind

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class StringBooleanSerializerTest {

    @Serializable
    data class ClassForTest(@Serializable(with = StringBooleanSerializer::class) val bar: Boolean)

    @Test
    fun deserialize() {
        assertTrue(Json.decodeFromString<ClassForTest>("""{"bar":"true"}""").bar)
        assertTrue(Json.decodeFromString<ClassForTest>("""{"bar":"True"}""").bar)
        assertTrue(Json.decodeFromString<ClassForTest>("""{"bar":"ja"}""").bar)
        assertTrue(Json.decodeFromString<ClassForTest>("""{"bar":"Ja"}""").bar)
        assertFalse(Json.decodeFromString<ClassForTest>("""{"bar":"false"}""").bar)
        assertFalse(Json.decodeFromString<ClassForTest>("""{"bar":"nei"}""").bar)
        assertThrows<IllegalArgumentException> { Json.decodeFromString<ClassForTest>("""{"bar":"hei"}""").bar }
        assertThrows<IllegalArgumentException> { Json.decodeFromString<ClassForTest>("""{"bar":null}""").bar }
        assertThrows<IllegalArgumentException> { Json.decodeFromString<ClassForTest>("""{"bar":42}""").bar }
        assertThrows<IllegalArgumentException> { Json.decodeFromString<ClassForTest>("""{"bar":true}""").bar }
        assertThrows<IllegalArgumentException> { Json.decodeFromString<ClassForTest>("""{"bar":false}""").bar }
    }
}
