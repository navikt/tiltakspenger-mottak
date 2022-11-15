package no.nav.tiltakspenger.mottak.saf

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.plugins.*
import mu.KotlinLogging
import no.nav.tiltakspenger.mottak.Configuration
import no.nav.tiltakspenger.mottak.HttpClient.client
import no.nav.tiltakspenger.mottak.INDIVIDSTONAD
import no.nav.tiltakspenger.mottak.saf.SafQuery.Variantformat.ORIGINAL

private val SECURELOG = KotlinLogging.logger("tjenestekall")

class SafClient(private val config: Configuration.SafConfig, private val getToken: suspend () -> String) {
    companion object {
        private const val FILNAVN = "tiltakspenger.json"
    }

    suspend fun hentMetadataForJournalpost(journalpostId: String): JournalfortDokumentMetaData? {
        val token = getToken()
        val safResponse: SafQuery.Response = client.post(
            urlString = "${config.baseUrl}/graphql"
        ) {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            header("Tema", INDIVIDSTONAD)
            setBody(Graphql(journalpost(journalpostId)))
        }.body()

        if (safResponse.errors != null) throw NotFoundException("Feil fra SAF: ${safResponse.errors}")
        val journalpostResponse = safResponse.data?.journalpost
        return toJournalfortDokumentMetadata(journalpostResponse)
    }

    suspend fun hentSoknad(journalfortDokumentMetaData: JournalfortDokumentMetaData): String {
        val token = getToken()
        val journalpostId = journalfortDokumentMetaData.journalpostId
        val dokumentInfoId = journalfortDokumentMetaData.dokumentInfoId
        val safResponse: String = client.get(
            urlString = "${config.baseUrl}/rest/hentdokument/$journalpostId/$dokumentInfoId/$ORIGINAL"
        ) {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            header("Tema", INDIVIDSTONAD)
        }.bodyAsText()
        return safResponse
    }

    private fun toJournalfortDokumentMetadata(response: SafQuery.Journalpost?): JournalfortDokumentMetaData? {
        SECURELOG.info { "Metadata fra SAF: $response" }
        val dokumentInfoId = response?.dokumenter?.firstOrNull { dokument ->
            dokument.dokumentvarianter.any { it.filnavn == FILNAVN && it.variantformat == ORIGINAL }
        }?.dokumentInfoId

        val vedlegg = response?.dokumenter?.filterNot { dokument ->
            dokument.dokumentvarianter.any { it.filnavn == FILNAVN || it.filnavn == "L7" }
        }?.map {
            VedleggMetaData(
                journalpostId = response.journalpostId,
                dokumentInfoId = it.dokumentInfoId,
                filnavn = it.dokumentvarianter.firstOrNull()?.filnavn
            )
        } ?: emptyList()

        return if (dokumentInfoId == null) null else JournalfortDokumentMetaData(
            journalpostId = response.journalpostId,
            dokumentInfoId = dokumentInfoId,
            filnavn = FILNAVN,
            vedlegg = vedlegg
        )
    }
}
