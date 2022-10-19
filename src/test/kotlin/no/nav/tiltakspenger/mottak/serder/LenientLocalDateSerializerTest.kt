package no.nav.tiltakspenger.mottak.serder

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.time.LocalDate

internal class LenientLocalDateSerializerTest {

    @Serializable
    data class ClassForTest(@Serializable(with = LenientLocalDateSerializer::class) val dato: LocalDate?)

    @Test
    fun `Serializes null`() {
        // given
        val classForTest = ClassForTest(null)

        // when
        val json = Json.encodeToString(classForTest)

        // then
        JSONAssert.assertEquals("""{dato:null}""", json, JSONCompareMode.LENIENT)
    }

    @Test
    fun `Returns 'null' when deserializer can not parse`() {
        // given
        val json = """{"dato":"NaN-aN-aN"}"""
        // when
        val classForTest = Json.decodeFromString<ClassForTest>(json)

        // then
        assertNull(classForTest.dato)
    }
}
