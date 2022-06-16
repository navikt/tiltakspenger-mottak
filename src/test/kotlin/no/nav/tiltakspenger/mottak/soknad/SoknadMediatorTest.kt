package no.nav.tiltakspenger.mottak.soknad

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import no.nav.tiltakspenger.mottak.clients.AzureOauthClient
import no.nav.tiltakspenger.mottak.clients.saf.SafClient
import no.nav.tiltakspenger.mottak.db.queries.PersonQueries
import no.nav.tiltakspenger.mottak.graphql.JournalfortDokumentMetaData
import org.junit.jupiter.api.Test

internal class SoknadMediatorTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when no metadata is found, no document is retreived`() = runTest {
        // given
        val journalpostId = "42"
        mockkObject(AzureOauthClient)
        coEvery { AzureOauthClient.getToken() } returns "TOKEN"
        mockkObject(SafClient)
        coEvery { SafClient.hentMetadataForJournalpost(journalpostId) }.returns(null)

        // when
        handleSoknad(journalpostId)

        // then
        coVerify(exactly = 1) { SafClient.hentMetadataForJournalpost(journalpostId) }
        coVerify(exactly = 0) { SafClient.hentSoknad(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when metadata is found, a document is retreived`() = runTest {
        // given
        val journalpostId = "42"
        val dokumentInfoId = "43"
        val rawJson = this::class.java.classLoader.getResource("mocksoknad.json")!!.readText()
        mockkObject(AzureOauthClient)
        coEvery { AzureOauthClient.getToken() } returns "TOKEN"
        mockkObject(SafClient)
        val journalfortDokumentMetaData = JournalfortDokumentMetaData(
            journalpostId = journalpostId,
            dokumentInfoId = dokumentInfoId,
            filnavn = "filnavn"
        )
        coEvery { SafClient.hentMetadataForJournalpost(journalpostId) }.returns(
            journalfortDokumentMetaData
        )
        coEvery { SafClient.hentSoknad(journalfortDokumentMetaData) }.returns(rawJson)
        mockkObject(SoknadQueries)
        coEvery { SoknadQueries.insertIfNotExists(any(), any(), any(), any()) } returns Unit
        mockkObject(PersonQueries)
        coEvery { PersonQueries.insertIfNotExists(any(), any(), any()) } returns Unit

        // when
        handleSoknad(journalpostId)

        // then
        coVerify(exactly = 1) { SafClient.hentMetadataForJournalpost(journalpostId) }
        coVerify(exactly = 1) { SafClient.hentSoknad(journalfortDokumentMetaData) }
        coVerify(exactly = 1) { PersonQueries.insertIfNotExists(any(), any(), any()) }
        coVerify(exactly = 1) {
            SoknadQueries
                .insertIfNotExists(journalpostId.toInt(), dokumentInfoId.toInt(), rawJson, any())
        }
    }
}
