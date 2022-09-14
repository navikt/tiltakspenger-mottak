package no.nav.tiltakspenger.mottak.søknad

import mu.KotlinLogging
import no.nav.tiltakspenger.mottak.clients.saf.SafClient

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

suspend fun handleSøknad(journalpostId: String): Søknad? {
    LOG.info { "Retrieving journalpost metadata with journalpostId $journalpostId" }
    val journalfortDokumentMetaData = SafClient.hentMetadataForJournalpost(journalpostId)
    return if (journalfortDokumentMetaData != null) {
        LOG.info { "Retrieving søknad with dokumentInfoId ${journalfortDokumentMetaData.dokumentInfoId}" }
        val json = SafClient.hentSoknad(journalfortDokumentMetaData)
        LOG.info {
            "Retrieved søknad with dokumentInfoId " +
                    "${journalfortDokumentMetaData.dokumentInfoId}, see secure-log for details"
        }
        SECURELOG.info { "Retrieved søknad $json" }
        Søknad.fromJson(json, journalpostId, journalfortDokumentMetaData.dokumentInfoId)
    } else {
        LOG.info { "Journalpost with ID $journalpostId was not handled" }
        null
    }
}
