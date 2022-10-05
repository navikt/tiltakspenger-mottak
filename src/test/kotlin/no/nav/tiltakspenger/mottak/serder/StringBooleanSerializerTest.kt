package no.nav.tiltakspenger.mottak.serder

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
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
        assertThrows<SerializationException> { Json.decodeFromString<ClassForTest>("""{"bar":"hei"}""") }
        assertThrows<SerializationException> { Json.decodeFromString<ClassForTest>("""{"bar":null}""") }
        assertThrows<SerializationException> { Json.decodeFromString<ClassForTest>("""{"bar":42}""") }
        assertThrows<SerializationException> { Json.decodeFromString<ClassForTest>("""{"bar":true}""") }
        assertThrows<SerializationException> { Json.decodeFromString<ClassForTest>("""{"bar":false}""") }
        assertThrows<SerializationException> { Json.decodeFromString<ClassForTest>("ikke json") }
    }
}
