package no.nav.tiltakspenger.mottak.saf

import mu.KotlinLogging
import no.nav.tiltakspenger.mottak.Configuration
import no.nav.tiltakspenger.mottak.søknad.Søknad

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

object SafService {
    private val safClient = SafClient(Configuration.SafConfig())
    suspend fun hentSøknad(journalpostId: String): Søknad? {
        LOG.info { "Retrieving journalpost metadata with journalpostId $journalpostId" }
        val journalfortDokMetadata = safClient.hentMetadataForJournalpost(journalpostId)
        if (journalfortDokMetadata == null) {
            LOG.info { "Journalpost with ID $journalpostId was not handled" }
            return null
        }
        LOG.info { "Retrieving søknad with dokumentInfoId ${journalfortDokMetadata.dokumentInfoId}" }
        val json = safClient.hentSoknad(journalfortDokMetadata)
        LOG.info {
            "Retrieved søknad with dokumentInfoId ${journalfortDokMetadata.dokumentInfoId}, see secure-log for details"
        }
        SECURELOG.info { "Retrieved søknad $json" }
        return Søknad.fromJson(json, journalpostId, journalfortDokMetadata.dokumentInfoId)
    }
}
