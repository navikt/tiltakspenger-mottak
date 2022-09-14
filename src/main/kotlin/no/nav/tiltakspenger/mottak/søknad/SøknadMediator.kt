package no.nav.tiltakspenger.mottak.søknad

import mu.KotlinLogging
import no.nav.tiltakspenger.mottak.saf.SafClient

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

suspend fun handleSøknad(journalpostId: String): Søknad? {
    LOG.info { "Retrieving journalpost metadata with journalpostId $journalpostId" }
    val journalfortDokumentMetaData = SafClient.hentMetadataForJournalpost(journalpostId)
    if (journalfortDokumentMetaData == null) {
        LOG.info { "Journalpost with ID $journalpostId was not handled" }
        return null
    }
    LOG.info { "Retrieving søknad with dokumentInfoId ${journalfortDokumentMetaData.dokumentInfoId}" }
    val json = SafClient.hentSoknad(journalfortDokumentMetaData)
    LOG.info {
        "Retrieved søknad with dokumentInfoId ${journalfortDokumentMetaData.dokumentInfoId}, see secure-log for details"
    }
    SECURELOG.info { "Retrieved søknad $json" }
    return Søknad.fromJson(json, journalpostId, journalfortDokumentMetaData.dokumentInfoId)
}
