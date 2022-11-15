package no.nav.tiltakspenger.mottak.saf

import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import no.nav.tiltakspenger.mottak.Configuration
import no.nav.tiltakspenger.mottak.HttpClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

internal class SafClientTest {
    private val safClient = SafClient(Configuration.SafConfig()) { "a token to be used for tests" }

    private fun mockSafRequest(filnavn: String) {
        val mockEngine = MockEngine {
            respond(
                content = javaClass.getResource(filnavn)?.readText(Charsets.UTF_8)!!,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        mockkObject(HttpClient)
        every { HttpClient.client } returns io.ktor.client.HttpClient(mockEngine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
    }

    @AfterEach
    fun clear() {
        clearAllMocks()
    }

    @Test
    fun `skal lage request til saf graphql og parse responsen`() {
        mockSafRequest("/journalpost_med_filnavn.json")
        val safResponse = runBlocking {
            safClient.hentMetadataForJournalpost("524272526")
        }

        assertEquals("524272526", safResponse?.journalpostId)
        assertEquals("tiltakspenger.json", safResponse?.filnavn)
        assertEquals("548464748", safResponse?.dokumentInfoId)
    }

    @Test
    fun `ignorerer søknad som har filnavn==null`() {
        mockSafRequest("/journalpost_med_null_filnavn.json")
        val safResponse = runBlocking {
            safClient.hentMetadataForJournalpost("524989475")
        }

        assertNull(safResponse)
    }

    @Test
    fun `ignorerer søknad som ikke er en faktisk søknad`() {
        mockSafRequest("/journalpost_med_ettersendelse.json")
        val safResponse = runBlocking {
            safClient.hentMetadataForJournalpost("524975813")
        }

        assertNull(safResponse)
    }

    @Test
    fun `hente dokument fra SAF`() {
        mockSafRequest("/søknad.json")
        val jsonSoknad = javaClass.getResource("/søknad.json")?.readText(Charsets.UTF_8)!!
        val journalfortDokumentMetadata = JournalfortDokumentMetadata("524272526", "2", "tittel")

        val safResponse = runBlocking {
            safClient.hentSoknad(journalfortDokumentMetadata)
        }

        JSONAssert.assertEquals(jsonSoknad, safResponse, JSONCompareMode.LENIENT)
    }

    @Test
    fun `hente vedlegg fra Metadata`() {
        mockSafRequest("/journalpost_med_vedlegg.json")
        val safResponse = runBlocking {
            safClient.hentMetadataForJournalpost("524272526")
        }

        val vedlegg = safResponse?.vedlegg!!

        assertEquals(vedlegg.size, 1)
        assertEquals(vedlegg.first().dokumentInfoId, "549168778")
        assertEquals(vedlegg.first().filnavn, "Vedlegg 2")
        assertEquals(vedlegg.first().journalpostId, "524272526")
    }
}
