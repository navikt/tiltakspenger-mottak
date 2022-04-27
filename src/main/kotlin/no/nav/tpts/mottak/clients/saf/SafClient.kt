package no.nav.tpts.mottak.clients.saf

import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.server.plugins.NotFoundException
import no.nav.tpts.mottak.clients.AzureOauthClient.getToken
import no.nav.tpts.mottak.clients.HttpClient.client
import no.nav.tpts.mottak.getSafUrl
import no.nav.tpts.mottak.graphql.Graphql
import no.nav.tpts.mottak.graphql.JournalfortDokumentMetaData
import no.nav.tpts.mottak.graphql.SafQuery
import no.nav.tpts.mottak.graphql.SafQuery.Variantformat.ORIGINAL
import no.nav.tpts.mottak.graphql.journalpost

object SafClient {
    private const val FILNAVN = "tiltakspenger.json"
    private const val INDIVIDSTONAD = "IND"
    private val safUrl = getSafUrl()

    suspend fun hentMetadataForJournalpost(journalpostId: String): JournalfortDokumentMetaData? {
        val token = getToken()
        val safResponse: SafQuery.Response = client.post(
            urlString = "$safUrl/graphql"
        ) {
            bearerAuth(token)
            accept(ContentType.Application.Json)
            header("Tema", INDIVIDSTONAD)
            contentType(ContentType.Application.Json)
            setBody(Graphql(journalpost(journalpostId)))
        }.body()

        if (safResponse.errors != null) {
            throw NotFoundException(
                "Det oppsto en feil ved å hente data fra SAF graphql. Message: ${safResponse.errors[0].message}"
            )
        }
        val journalPostResponse = safResponse.data?.journalpost
        return toJournalfortDokumentMetadata(journalPostResponse)
    }

    suspend fun hentSoknad(journalfortDokumentMetaData: JournalfortDokumentMetaData): String {
        val token = getToken()
        val journalpostId = journalfortDokumentMetaData.journalpostId
        val dokumentInfoId = journalfortDokumentMetaData.dokumentInfoId
        val safResponse: String = client.get(
            urlString = "$safUrl/rest/hentdokument/$journalpostId/$dokumentInfoId/$ORIGINAL"
        ) {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            header("Tema", INDIVIDSTONAD)
        }.bodyAsText()
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
