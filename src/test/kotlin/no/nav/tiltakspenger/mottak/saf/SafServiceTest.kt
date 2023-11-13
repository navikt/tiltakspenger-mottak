package no.nav.tiltakspenger.mottak.saf

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SafServiceTest {
    private val safClient = mockk<SafClient>()
    private val safService = SafService(safClient)

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when no metadata is found, no document is retreived`() = runTest {
        // given
        val journalpostId = "42"
        coEvery { safClient.hentMetadataForJournalpost(journalpostId) }.returns(null)

        // when
        val soknad = safService.hentSøknad(journalpostId)

        // then
        assertNull(soknad)
        coVerify(exactly = 1) { safClient.hentMetadataForJournalpost(journalpostId) }
        coVerify(exactly = 0) { safClient.hentSoknad(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when versjonnr is found we map correct versjon`() = runTest {
        // given
        val journalpostId = "42"
        val dokumentInfoId = "43"
        val rawJson = this::class.java.classLoader.getResource("ny_søknad.json")!!.readText()
        val journalfortDokumentMetadata = JournalfortDokumentMetadata(
            journalpostId = journalpostId,
            dokumentInfoId = dokumentInfoId,
            filnavn = "tiltakspengersoknad.json",
        )
        coEvery { safClient.hentMetadataForJournalpost(journalpostId) }.returns(
            journalfortDokumentMetadata,
        )
        coEvery { safClient.hentSoknad(journalfortDokumentMetadata) }.returns(rawJson)

        // when
        val soknad = safService.hentSøknad(journalpostId)

//        val mappedJson = jacksonObjectMapper().readTree(soknad?.søknad) as ObjectNode

        // then
//        assertEquals("12304", mappedJson.path("søknadId").asText())
        assertEquals("12304", soknad?.søknadId)
        coVerify(exactly = 1) { safClient.hentMetadataForJournalpost(journalpostId) }
        coVerify(exactly = 1) { safClient.hentSoknad(journalfortDokumentMetadata) }
    }
}
