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

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class SafClient(private val config: Configuration.SafConfig, private val getToken: suspend () -> String) {
    companion object {
        private const val FILNAVN_SØKNAD = "tiltakspenger.json"
        private const val FILNAVN_KVITTERINGSSIDE = "L7"
    }

    suspend fun hentMetadataForJournalpost(journalpostId: String): JournalfortDokumentMetadata? {
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

    suspend fun hentSoknad(journalfortDokumentMetadata: JournalfortDokumentMetadata): String {
        val token = getToken()
        val journalpostId = journalfortDokumentMetadata.journalpostId
        val dokumentInfoId = journalfortDokumentMetadata.dokumentInfoId
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

    private fun toJournalfortDokumentMetadata(response: SafQuery.Journalpost?): JournalfortDokumentMetadata? {
        SECURELOG.info { "Metadata fra SAF: $response" }
        LOG.info {
            "Dokumenter fra SAF: ${
                response?.dokumenter?.joinToString {
                    "${it.tittel}: [${it.dokumentvarianter.map { v -> v.filnavn }.joinToString()}]"
                }
            }"
        }
        val dokumentInfoId = response?.dokumenter?.firstOrNull { dokument ->
            dokument.dokumentvarianter.any { it.filnavn == FILNAVN_SØKNAD && it.variantformat == ORIGINAL }
        }?.dokumentInfoId

        val vedlegg = response?.dokumenter?.filterNot { dokument ->
            dokument.dokumentvarianter.any { it.filnavn == FILNAVN_SØKNAD || it.filnavn == FILNAVN_KVITTERINGSSIDE }
        }?.map {
            VedleggMetadata(
                journalpostId = response.journalpostId,
                dokumentInfoId = it.dokumentInfoId,
                filnavn = it.dokumentvarianter.firstOrNull()?.filnavn
            )
        } ?: emptyList()

        return if (dokumentInfoId == null) null else JournalfortDokumentMetadata(
            journalpostId = response.journalpostId,
            dokumentInfoId = dokumentInfoId,
            filnavn = FILNAVN_SØKNAD,
            vedlegg = vedlegg
        )
    }
}
