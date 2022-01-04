package no.nav.tpts.mottak

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.nav.tpts.mottak.joark.models.SoknadRaw
import org.junit.jupiter.api.Test

class SoknadTest {

    @Test
    fun shouldSerializeRawSoknadsData() {
        val rawJsonSoknad = javaClass.getResource("/mocksoknad1.json")?.readText(Charsets.UTF_8)!!
        Json.decodeFromString<SoknadRaw>(rawJsonSoknad)
    }
}
