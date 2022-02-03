package no.nav.tpts.mottak.saf;

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import no.nav.tpts.mottak.clients.saf.SafClient
import no.nav.tpts.mottak.clients.saf.journalpost
import org.junit.jupiter.api.Test;

@WireMockTest
class SafClientImplTest {

    @Test
    suspend fun `skal lage request til saf graphql og parse responsen`() {

        givenThat(
            post(urlEqualTo("/graphql"))
                .withHeader("Authorization", equalTo("Bearer TOKEN"))
                .withHeader("Tema", equalTo("IND"))
                .withRequestBody(
                    equalToJson(
                        """
							{
								"query": ${journalpost("524272526")}
                            }
                            """
                    )
                )
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody(
                            """
								{
									"errors": null,
									"data": {
										"journalpost": {
                                        "journalpostId": "524272526",
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
                        )
                )
        )

        val jsonSoknadInfo = SafClient.hentMetadataForJournalpost("524272526")

        /*Assertions.assertEquals("548464748", jsonSoknadInfo.dokumentInfoId)
        Assertions.assertEquals("524272526", jsonSoknadInfo.journalpostId)
        Assertions.assertEquals("Søknad om tiltakspenger", jsonSoknadInfo.dokumentTittel)*/
    }
}
