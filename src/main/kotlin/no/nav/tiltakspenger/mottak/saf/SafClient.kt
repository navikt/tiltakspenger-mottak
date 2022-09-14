package no.nav.tiltakspenger.mottak.saf

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.plugins.*
import no.nav.tiltakspenger.mottak.clients.AzureOauthClient.getToken
import no.nav.tiltakspenger.mottak.clients.HttpClient.client
import no.nav.tiltakspenger.mottak.getSafUrl
import no.nav.tiltakspenger.mottak.saf.SafQuery.Variantformat.ORIGINAL

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

        if (safResponse.errors != null) throw NotFoundException("Error from SAF: ${safResponse.errors}")
        val journalpostResponse = safResponse.data?.journalpost
        return toJournalfortDokumentMetadata(journalpostResponse)
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
