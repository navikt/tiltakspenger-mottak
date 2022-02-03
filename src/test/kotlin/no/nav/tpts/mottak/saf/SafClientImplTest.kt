package no.nav.tpts.mottak.saf

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.http.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import no.nav.tpts.mottak.clients.AzureOauthClient
import no.nav.tpts.mottak.clients.HttpClient
import no.nav.tpts.mottak.clients.OAuth2AccessTokenResponse
import no.nav.tpts.mottak.clients.saf.SafClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SafClientImplTest {

    private companion object {
        val JOURNALPOST_ID = "524272526"

        val journalpostJson = """
            {
                "errors": null,
                "data": {
                    "journalpost": {
                        "journalpostId": "$JOURNALPOST_ID",
                        "tittel": "Søknad om tiltakspenger",
                        "dokumenter": [
                        {
                            "dokumentInfoId": "548464748",
                            "tittel": "Søknad om tiltakspenger",
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

    @Test
    fun `skal lage request til saf graphql og parse responsen`() {

        mockkObject(AzureOauthClient)
        coEvery { AzureOauthClient.getToken() } returns OAuth2AccessTokenResponse("TOKEN", "ACCESS_TOKEN", 123, 123)

        val mockEngine = MockEngine {
            respond(
                content = journalpostJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        mockkObject(HttpClient)
        every { HttpClient.httpClient } returns io.ktor.client.HttpClient(mockEngine) {
            install(JsonFeature) {
                serializer = KotlinxSerializer()
            }
        }

        val safResponse = runBlocking {
            SafClient.hentMetadataForJournalpost(JOURNALPOST_ID)
        }

        assertEquals(JOURNALPOST_ID, safResponse.journalpostId)
        assertEquals("Søknad om tiltakspenger", safResponse.dokumentTittel)
        assertEquals("548464748", safResponse.dokumentInfoId)
    }
}
