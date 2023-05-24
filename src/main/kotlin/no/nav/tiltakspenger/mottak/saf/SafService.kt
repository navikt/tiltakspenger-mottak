package no.nav.tiltakspenger.mottak.saf

import mu.KotlinLogging
import no.nav.tiltakspenger.mottak.søknad.Søknad

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class SafService(private val safClient: SafClient) {

    companion object {
        private const val FILNAVN_NY_SØKNAD = "tiltakspengersoknad.json"
    }

    suspend fun hentSøknad(journalpostId: String): Søknad? {
        LOG.info { "Henter metadata for journalpost med journalpostId $journalpostId" }
        val metadata = safClient.hentMetadataForJournalpost(journalpostId)
        if (metadata == null) {
            LOG.info { "Journalpost med id $journalpostId ble ikke håndtert" }
            return null
        }
        LOG.info { "Henter søknad med dokumentInfoId ${metadata.dokumentInfoId}" }
        if (metadata.filnavn == FILNAVN_NY_SØKNAD) {
            LOG.info { "Journalpost med id $journalpostId ignoreres, er ny søknad" }
            return null
        }
        val json = safClient.hentSoknad(metadata)
        LOG.info { "Hentet søknad med dokumentInfoId ${metadata.dokumentInfoId}, se secure-log for detaljer" }
        SECURELOG.info { "Hentet søknad $json" }
        return Søknad.fromJson(json, journalpostId, metadata.dokumentInfoId, metadata.vedlegg)
    }
}
