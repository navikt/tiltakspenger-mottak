package no.nav.tiltakspenger.mottak.søknad.models

import no.nav.tiltakspenger.mottak.søknad.DokumentInfoDTO
import no.nav.tiltakspenger.mottak.søknad.SøknadDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class JoarkSøknadTest {
    val dokInfo = DokumentInfoDTO(
        journalpostId = "journalpostId",
        dokumentInfoId = "dokumentInfoId",
        filnavn = "tiltakspenger.json",
    )

    @Test
    fun `from ny søknad som feiler med mangler ident`() {
        val faktums = this::class.java.classLoader.getResource("ny_søknad.json")!!.readText()
        SøknadDTO.fromSøknadV4(faktums, dokInfo).also {
            assertEquals("NØDVENDIG", it.personopplysninger.fornavn)
            assertEquals("HOFTE", it.personopplysninger.etternavn)
            assertEquals("09877698987", it.personopplysninger.ident)
        }
    }
}
