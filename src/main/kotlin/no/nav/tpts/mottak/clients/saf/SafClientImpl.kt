package no.nav.tpts.mottak.clients.saf

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.request.*
import io.ktor.http.*
import java.util.function.Supplier
import okhttp3.Request
import okhttp3.RequestBody
import no.nav.tpts.mottak.clients.saf.SafQuery.journalpost.Variantformat.ORIGINAL
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient

class SafClientImpl(
    private val tokenProvider: Supplier<String>,
    private val safUrl: String,
    private val httpClient: OkHttpClient = OkHttpClient(),
    private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())
): SafClient {

    override fun hentMetadataForJournalpost(journalpostId: String): JournalfortDokumentMetaData {

        val jsonObject = objectMapper.writeValueAsString(
            Graphql.GraphqlQuery(
                SafQuery.journalpost.query,
                SafQuery.journalpost.Variables(journalpostId)
            )
        )

        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonObject)

        val request = Request.Builder()
            .url("$safUrl/graphql")
            .addHeader("Authorization", "Bearer ${tokenProvider.get()}")
            .addHeader("Tema", "IND")
            .post(requestBody)
            .build()

        httpClient.newCall(request).execute().use { response ->
            if(!response.isSuccessful) {
                throw RuntimeException("Klarte ikke å hente data fra SAF. Status: ${response.code}")
            }

            val body = response.body?.string() ?: throw RuntimeException("Body is missing from SAF response")

            val gqlResponse = objectMapper.readValue(body, SafQuery.journalpost.Response::class.java)

            if(gqlResponse.data == null) {
                throw RuntimeException("SAF response inneholder ikke data")
            }

            return toJournalfortDokumentMetadata(gqlResponse.data)
        }
    }

    private fun toJournalfortDokumentMetadata(response: SafQuery.journalpost.ResponseData): JournalfortDokumentMetaData {
        val journalpostId = response.journalpost.journalpostId
        val dokumenter = response.journalpost.dokumenter
        val dokument = dokumenter.stream().filter { it.tittel == "Søknad om tiltakspenger" }.findFirst()
        val dokumentTittel = dokument.get().tittel
        val dokumentInfoId = dokument
            .ifPresent { dok ->
                dok.dokumentvarianter.stream()
                    .filter { it.variantformat == ORIGINAL }
                    .findFirst().ifPresent { dok.dokumentInfoId }
            }.toString()

        return JournalfortDokumentMetaData(
            journalpostId = journalpostId,
            dokumentInfoId = dokumentInfoId,
            dokumentTittel = dokumentTittel,
        )
    }
}
