package no.nav.tpts.mottak.clients.saf

import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.features.NotFoundException
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import no.nav.tpts.mottak.clients.AzureOauthClient.getToken
import no.nav.tpts.mottak.clients.HttpClient.client
import no.nav.tpts.mottak.getSafUrl
import no.nav.tpts.mottak.graphql.Graphql
import no.nav.tpts.mottak.graphql.JournalfortDokumentMetaData
import no.nav.tpts.mottak.graphql.SafQuery
import no.nav.tpts.mottak.graphql.SafQuery.Variantformat.ORIGINAL
import no.nav.tpts.mottak.graphql.journalpost

object SafClient {
    private val token = runBlocking { getToken().accessToken }

    suspend fun hentMetadataForJournalpost(journalpostId: String): JournalfortDokumentMetaData {
        val safResponse: SafQuery.Response = client.post(url = Url("{${getSafUrl()}/graphql}")) {
            header(HttpHeaders.Authorization, "(Bearer $token")
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

    fun toJournalfortDokumentMetadata(response: SafQuery.Journalpost?): JournalfortDokumentMetaData {

        val journalpostId = response?.journalpostId
        val dokument = response?.dokumenter?.first { it.tittel == "Søknad om tiltakspenger" }

        dokument?.dokumentvarianter?.firstOrNull { it.variantformat == ORIGINAL }
            ?: throw NotFoundException("Dokument finnes ikke i json format")

        val dokumentTittel = dokument.tittel
        val dokumentInfoId = dokument.dokumentInfoId

        return JournalfortDokumentMetaData(
            journalpostId = journalpostId,
            dokumentInfoId = dokumentInfoId,
            dokumentTittel = dokumentTittel,
        )
    }
}
