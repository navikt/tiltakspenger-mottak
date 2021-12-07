package no.nav.tpts.mottak

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.nav.tpts.mottak.joark.models.SoknadRaw
import kotlin.test.Test

class SoknadTest {

    @Test
    fun shouldSerializeRawSoknadsData() {
        val rawJsonSoknad = javaClass.getResource("/mocksoknad1.json")?.readText(Charsets.UTF_8)!!
        val obj = Json.decodeFromString<SoknadRaw>(rawJsonSoknad)
        println(obj)
    }

}