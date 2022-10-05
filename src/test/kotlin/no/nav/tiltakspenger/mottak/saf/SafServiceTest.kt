package no.nav.tiltakspenger.mottak.saf

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import no.nav.tiltakspenger.mottak.clients.AzureOauthClient
import no.nav.tiltakspenger.mottak.saf.SafService.hentSøknad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class SafServiceTest {
    private val safClient = mockk<SafClient>()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when no metadata is found, no document is retreived`() = runTest {
        // given
        val journalpostId = "42"
        mockkObject(AzureOauthClient)
        coEvery { AzureOauthClient.getToken() } returns "TOKEN"
        coEvery { safClient.hentMetadataForJournalpost(journalpostId) }.returns(null)

        // when
        // TODO: må mocke bort safClient i SafService
        val soknad = hentSøknad(journalpostId)

        // then
        assertNull(soknad)
        coVerify(exactly = 1) { safClient.hentMetadataForJournalpost(journalpostId) }
        coVerify(exactly = 0) { safClient.hentSoknad(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when metadata is found, a document is retreived`() = runTest {
        // given
        val journalpostId = "42"
        val dokumentInfoId = "43"
        val rawJson = this::class.java.classLoader.getResource("søknad.json")!!.readText()
        mockkObject(AzureOauthClient)
        coEvery { AzureOauthClient.getToken() } returns "TOKEN"
        val journalfortDokumentMetaData = JournalfortDokumentMetaData(
            journalpostId = journalpostId,
            dokumentInfoId = dokumentInfoId,
            filnavn = "filnavn"
        )
        coEvery { safClient.hentMetadataForJournalpost(journalpostId) }.returns(
            journalfortDokumentMetaData
        )
        coEvery { safClient.hentSoknad(journalfortDokumentMetaData) }.returns(rawJson)

        // when
        val soknad = hentSøknad(journalpostId)

        // then
        assertEquals("12304", soknad?.søknadId)
        coVerify(exactly = 1) { safClient.hentMetadataForJournalpost(journalpostId) }
        coVerify(exactly = 1) { safClient.hentSoknad(journalfortDokumentMetaData) }
    }
}
