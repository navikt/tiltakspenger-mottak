package no.nav.tpts.mottak.clients.saf

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.features.NotFoundException
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.runBlocking
import no.nav.tpts.mottak.clients.AzureOauthClient.getToken
import no.nav.tpts.mottak.clients.HttpClient.client
import no.nav.tpts.mottak.clients.Scope
import no.nav.tpts.mottak.getSafUrl
import no.nav.tpts.mottak.graphql.Graphql
import no.nav.tpts.mottak.graphql.JournalfortDokumentMetaData
import no.nav.tpts.mottak.graphql.SafQuery
import no.nav.tpts.mottak.graphql.SafQuery.Variantformat.ORIGINAL
import no.nav.tpts.mottak.graphql.journalpost

const val FILNAVN = "tiltakspenger.json"

object SafClient {
    private val token = runBlocking { getToken(Scope.SAF) }
    private val safUrl = getSafUrl()

    suspend fun hentMetadataForJournalpost(journalpostId: String): JournalfortDokumentMetaData? {
        val safResponse: SafQuery.Response = client.post(
            urlString = "$safUrl/graphql"
        ) {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.Accept, "application/json")
            header("Tema", "IND")
            header(HttpHeaders.ContentType, "application/json")

            body = Graphql(journalpost(journalpostId))
        }

        if (safResponse.errors != null) {
            throw NotFoundException(
                "Det oppsto en feil ved å hente data fra SAF graphql. Message: ${safResponse.errors[0].message}"
            )
        }

        val journalPostResponse = safResponse.data?.journalpost

        return toJournalfortDokumentMetadata(journalPostResponse)
    }

    suspend fun hentSoknad(journalfortDokumentMetaData: JournalfortDokumentMetaData): String {
        val journalpostId = journalfortDokumentMetaData.journalpostId
        val dokumentInfoId = journalfortDokumentMetaData.dokumentInfoId

        val safResponse: String = client.get(
            urlString = "$safUrl/rest/hentdokument/$journalpostId/$dokumentInfoId/$ORIGINAL"
        ) {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, "application/json")
            header(HttpHeaders.Accept, "application/json")
            header("Tema", "IND")
        }
        return safResponse
    }

    private fun toJournalfortDokumentMetadata(response: SafQuery.Journalpost?): JournalfortDokumentMetaData? {
        val dokumentInfoId = response?.dokumenter?.firstOrNull { dokument ->
            dokument.dokumentvarianter.any { it.filnavn == FILNAVN && it.variantformat == ORIGINAL }
        }?.dokumentInfoId

        return if (dokumentInfoId == null) null else JournalfortDokumentMetaData(
            journalpostId = response.journalpostId,
            dokumentInfoId = dokumentInfoId,
            filnavn = FILNAVN
        )
    }
}
