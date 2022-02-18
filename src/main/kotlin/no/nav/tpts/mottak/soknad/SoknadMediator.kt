package no.nav.tpts.mottak.soknad

import mu.KotlinLogging
import no.nav.tpts.mottak.clients.saf.SafClient

val LOG = KotlinLogging.logger {}

suspend fun handleSoknad(journalPostId: String) {
    LOG.info { "Retreiving søknad metadata with journalPostId $journalPostId" }
    val journalfortDokumentMetaData = SafClient.hentMetadataForJournalpost(journalPostId)

    if (journalfortDokumentMetaData != null) {
        LOG.info { "Retreiving søknad with dokumentInfoId ${journalfortDokumentMetaData.dokumentInfoId}" }
        val soknad = SafClient.hentSoknad(journalfortDokumentMetaData)
        LOG.info { "Retreived søknad $soknad" }

        LOG.debug { "Saving soknad to database" }
        // lagre soknad to database
    } else {
        LOG.info { "Soknad with journalpostID $journalPostId was not handled" }
    }
}
