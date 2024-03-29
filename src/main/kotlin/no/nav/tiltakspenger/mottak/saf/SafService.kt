package no.nav.tiltakspenger.mottak.saf

import mu.KotlinLogging
import no.nav.tiltakspenger.mottak.saf.SafClient.Companion.FILNAVN_NY_SØKNAD
import no.nav.tiltakspenger.mottak.søknad.DokumentInfoDTO
import no.nav.tiltakspenger.mottak.søknad.SøknadDTO

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class SafService(private val safClient: SafClient) {
    suspend fun hentSøknad(journalpostId: String): SøknadDTO? {
        LOG.info { "Henter metadata for journalpost med journalpostId $journalpostId" }
        val metadata = safClient.hentMetadataForJournalpost(journalpostId)
        if (metadata == null) {
            LOG.info { "Journalpost med id $journalpostId ble ikke håndtert" }
            return null
        }
        LOG.info { "Henter søknad med dokumentInfoId ${metadata.dokumentInfoId}" }
        val json = safClient.hentSoknad(metadata)
        LOG.info { "Hentet søknad med dokumentInfoId ${metadata.dokumentInfoId}, se secure-log for detaljer" }
        SECURELOG.info { "Hentet søknad $json" }

        if (metadata.filnavn == FILNAVN_NY_SØKNAD) {
            SECURELOG.info { "Vi mapper ny søknad" }

            return SøknadDTO.fromSøknadV4(
                json = json,
                dokInfo = DokumentInfoDTO(
                    journalpostId = journalpostId,
                    dokumentInfoId = metadata.dokumentInfoId,
                    filnavn = metadata.filnavn,
                ),
                vedleggMetadata = metadata.vedlegg,
            )
        }

        return null
    }
}
