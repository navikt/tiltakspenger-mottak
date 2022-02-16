package no.nav.tpts.mottak.soknad

import mu.KotlinLogging
import no.nav.tpts.mottak.clients.saf.SafClient

val LOG = KotlinLogging.logger {}

suspend fun handleSoknad(journalPostId: String): String {
    LOG.info { "Retrieving søknad metadata with journalPostId $journalPostId" }
    val journalfortDokumentMetaData = SafClient.hentMetadataForJournalpost(journalPostId)
    LOG.info { "Retrieved søknad metadata with journalfortDokumentMetaData $journalfortDokumentMetaData" }

    LOG.info { "Retrieving søknad with journalPostId $journalPostId" }
    val joarkSoknad = SafClient.hentSoknad(journalfortDokumentMetaData)
    LOG.info { "Retrieved søknad $joarkSoknad" }

    LOG.debug { "Saving soknad to database" }
    // lagre soknad to database
    return joarkSoknad
}
