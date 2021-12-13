package no.nav.tpts.mottak.saf.api

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import no.nav.tpts.mottak.clients.AzureOauthClient.getToken
import no.nav.tpts.mottak.clients.httpClient
import no.nav.tpts.mottak.saf.models.JournalPostResponse
import no.nav.tpts.mottak.saf.models.SoknadRaw

const val SAF_URL = "https://saf.dev-fss-pub.nais.io/"

object SAFClient {
    private val token = runBlocking { getToken().accessToken }

    suspend fun getJournalPost(journalpostId: String): JournalPostResponse {
        val response: JournalPostResponse = httpClient.post(url = Url("${SAF_URL}graphql")) {
            headers.append("Authorization", "Bearer $token")
            headers.append("Content-Type", "application/json")
            body = GraphqlQuery(getDocumentsQuery(journalpostId))
        }
        return response
    }

    suspend fun getJournalPostDocument(journalpostId: String, dokumentInfoId: String, variantformat: String = "ORIGINAL"): SoknadRaw {
        return httpClient.get(Url("${SAF_URL}rest/hentdokument/${journalpostId}/${dokumentInfoId}/${variantformat}")) {
            headers.append("Authorization", "Bearer $token")
            headers.append("Content-Type", "application/json")
        }
    }
}