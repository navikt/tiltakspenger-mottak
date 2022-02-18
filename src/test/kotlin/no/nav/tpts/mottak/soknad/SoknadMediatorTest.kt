package no.nav.tpts.mottak.soknad

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import no.nav.tpts.mottak.clients.AzureOauthClient
import no.nav.tpts.mottak.clients.saf.SafClient
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
        coVerify(exactly = 0) { SafClient.hentSoknad(any()) }
    }
}
