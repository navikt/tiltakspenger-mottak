package no.nav.tpts.mottak.soknad

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import no.nav.tpts.mottak.clients.AzureOauthClient
import no.nav.tpts.mottak.clients.saf.SafClient
import no.nav.tpts.mottak.graphql.JournalfortDokumentMetaData
import no.nav.tpts.mottak.soknad.soknadList.SoknadQueries
import no.nav.tpts.mottak.soknad.soknadList.insertSoknad
import org.junit.jupiter.api.Test

internal class SoknadMediatorKtTest {

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
        coEvery { SafClient.hentSoknad(journalfortDokumentMetaData) }.returns("foo")
        mockkStatic(SoknadQueries::insertSoknad)
        coEvery { SoknadQueries.insertSoknad(any(), any(), any()) } returns Unit

        // when
        handleSoknad(journalpostId)

        // then
        coVerify(exactly = 1) { SafClient.hentMetadataForJournalpost(journalpostId) }
        coVerify(exactly = 1) { SafClient.hentSoknad(journalfortDokumentMetaData) }
    }
}
