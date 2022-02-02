package no.nav.tpts.mottak.joark

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.nav.tpts.mottak.joark.models.JoarkSoknad
import no.nav.tpts.mottak.soknad.SoknadDetails
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JoarkSoknadTest {

    @Test
    fun `should put joark faktum data into soknad object`() {
        val faktums = this::class.java.classLoader.getResource("faktums.json")!!.readText()
        val joarkSoknad = Json.decodeFromString<JoarkSoknad>(faktums)
        SoknadDetails.fromJoarkSoknad(joarkSoknad).also {
            assertEquals("TVILSOM", it.fornavn)
            assertEquals("BÆREPOSE", it.etternavn)
            assertEquals("05097421139", it.fnr)
            assertEquals("136347575", it.tiltak?.id)
            assertEquals("Jobbklubb", it.tiltak?.navn)
            assertEquals("FRISK UTVIKLING AS", it.tiltak?.arrangoer)
            assertEquals("2021-12-03T13:49:44.543", it.opprettet.toString())
        }
    }
}
