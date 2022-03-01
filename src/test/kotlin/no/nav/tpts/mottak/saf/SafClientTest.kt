package no.nav.tpts.mottak.saf

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import no.nav.tpts.mottak.Scope
import no.nav.tpts.mottak.clients.AzureOauthClient
import no.nav.tpts.mottak.clients.HttpClient
import no.nav.tpts.mottak.clients.saf.SafClient
import no.nav.tpts.mottak.graphql.JournalfortDokumentMetaData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

class SafClientTest {
    private companion object {
        const val JOURNALPOST_ID = "524272526"

        val journalpostJson = """
            {
                "errors": null,
                "data": {
                    "journalpost": {
                        "journalpostId": "$JOURNALPOST_ID",
                        "dokumenter": [
                        {
                            "dokumentInfoId": "548464748",
                            "tittel": "en eller annen tittel",
                            "dokumentvarianter": [
                                {
                                    "variantformat": "ORIGINAL",
                                    "filnavn": "tiltakspenger.json",
                                    "filtype": "JSON"
                                },
                                {
                                  "variantformat": "ARKIV",
                                  "filnavn": "NAV 76-13.45.pdfa",
                                  "filtype": "PDF"
                                }
                            ]
                        },
                        {
                          "dokumentInfoId": "548464747",
                          "tittel": "Kvitteringsside for dokumentinnsending",
                          "dokumentvarianter": [
                            {
                              "variantformat": "ARKIV",
                              "filnavn": "L7",
                              "filtype": "PDF"
                            }
                          ]
                        }
                        ]
                    }
                }
            }
        """.trimIndent()
    }

    private fun mockSafRequest(mockJsonString: String) {
        mockkObject(AzureOauthClient)
        coEvery { AzureOauthClient.getToken(Scope.SAF) } returns "TOKEN"

        val mockEngine = MockEngine {
            respond(
                content = mockJsonString,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        mockkObject(HttpClient)
        every { HttpClient.client } returns io.ktor.client.HttpClient(mockEngine) {
            install(JsonFeature) {
                serializer = KotlinxSerializer(Json { ignoreUnknownKeys = true })
            }
        }
    }

    @Test
    fun `skal lage request til saf graphql og parse responsen`() {

        mockSafRequest(journalpostJson)
        val safResponse = runBlocking {
            SafClient.hentMetadataForJournalpost(JOURNALPOST_ID)
        }

        assertEquals(JOURNALPOST_ID, safResponse?.journalpostId)
        assertEquals("tiltakspenger.json", safResponse?.filnavn)
        assertEquals("548464748", safResponse?.dokumentInfoId)
    }

    @Test
    fun `ignorerer soknad som ikke er en faktisk soknad`() {
        val jsonSoknad = javaClass.getResource("/ettersendelse.json")?.readText(Charsets.UTF_8)!!
        mockSafRequest(jsonSoknad)
        val safResponse = runBlocking {
            SafClient.hentMetadataForJournalpost("524975813")
        }

        assertNull(safResponse)
    }

    @Test
    fun `hente dokument fra SAF`() {
        val jsonSoknad = javaClass.getResource("/mocksoknad.json")?.readText(Charsets.UTF_8)!!
        val journalfortDokumentMetaData = JournalfortDokumentMetaData(JOURNALPOST_ID, "2", "tittel")
        mockSafRequest(jsonSoknad)

        val safResponse = runBlocking {
            SafClient.hentSoknad(journalfortDokumentMetaData)
        }

        JSONAssert.assertEquals(jsonSoknad, safResponse, JSONCompareMode.LENIENT)
    }
}
