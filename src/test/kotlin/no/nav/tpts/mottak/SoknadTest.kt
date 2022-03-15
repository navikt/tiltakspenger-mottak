package no.nav.tpts.mottak

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.nav.tpts.mottak.joark.models.JoarkSoknad
import org.junit.jupiter.api.Test

internal class SoknadTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun shouldSerializeRawSoknadsData() {
        val rawJsonSoknad = javaClass.getResource("/mocksoknad1.json")?.readText(Charsets.UTF_8)!!
        json.decodeFromString<JoarkSoknad>(rawJsonSoknad)
    }
}
