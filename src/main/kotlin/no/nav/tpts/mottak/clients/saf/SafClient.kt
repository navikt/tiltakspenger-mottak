package no.nav.tpts.mottak.clients.saf

import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.tpts.mottak.clients.AzureOauthClient.getToken
import no.nav.tpts.mottak.clients.HttpClient.httpClient
import no.nav.tpts.mottak.graphql.Graphql
import no.nav.tpts.mottak.graphql.JournalfortDokumentMetaData
import no.nav.tpts.mottak.graphql.SafQuery
import no.nav.tpts.mottak.graphql.SafQuery.Variantformat.ORIGINAL
import no.nav.tpts.mottak.graphql.journalpost

const val SAF_URL = "https://saf.dev-fss-pub.nais.io"

object SafClient {
    private val token = runBlocking { getToken().accessToken }

    suspend fun hentMetadataForJournalpost(journalpostId: String): JournalfortDokumentMetaData {
        val safResponse: SafQuery.Response = httpClient.post(url = Url("$SAF_URL/graphql")) {
            header("Authorization", "(Bearer $token")
            header("Accept", "application/json")
            header("Tema", "IND")
            header("Content-Type", "application/json")

            body = Graphql(journalpost(journalpostId))
        }

        if (safResponse.errors?.get(0)?.message?.isNotEmpty() == true) {
            throw RuntimeException("Det oppsto en feil ved å hente data fra SAF graphql. Message: ${safResponse.errors[0].message}")
        }

        val journalPostResponse = safResponse.data?.journalpost

        return toJournalfortDokumentMetadata(journalPostResponse)
    }

    fun toJournalfortDokumentMetadata(response: SafQuery.Journalpost?): JournalfortDokumentMetaData {

        val journalpostId = response?.journalpostId
        val dokument = response?.dokumenter?.first { it.tittel == "Søknad om tiltakspenger" }

        dokument?.dokumentvarianter?.firstOrNull { it.variantformat == ORIGINAL } ?: throw RuntimeException("Dokument finnes ikke i json format")

        val dokumentTittel = dokument.tittel
        val dokumentInfoId = dokument.dokumentInfoId

        return JournalfortDokumentMetaData(
            journalpostId = journalpostId,
            dokumentInfoId = dokumentInfoId,
            dokumentTittel = dokumentTittel,
        )
    }
}
