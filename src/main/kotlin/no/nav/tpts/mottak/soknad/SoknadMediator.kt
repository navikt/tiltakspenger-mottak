package no.nav.tpts.mottak.soknad

import mu.KotlinLogging
import no.nav.tpts.mottak.clients.saf.SafClient
import no.nav.tpts.mottak.soknad.soknadList.SoknadQueries
import no.nav.tpts.mottak.soknad.soknadList.insertSoknad

val LOG = KotlinLogging.logger {}

suspend fun handleSoknad(journalPostId: String) {
    LOG.info { "Retrieving journalpost metadata with journalPostId $journalPostId" }
    val journalfortDokumentMetaData = SafClient.hentMetadataForJournalpost(journalPostId)

    if (journalfortDokumentMetaData != null) {
        LOG.info { "Retrieving søknad with dokumentInfoId ${journalfortDokumentMetaData.dokumentInfoId}" }
        val soknad = SafClient.hentSoknad(journalfortDokumentMetaData)
        LOG.info { "Retrieved søknad $soknad" }

        LOG.debug { "Saving soknad to database" }
        // lagre soknad to database
        SoknadQueries.insertSoknad(journalPostId.toInt(), journalfortDokumentMetaData.dokumentInfoId?.toInt(), soknad)
    } else {
        LOG.info { "Journalpost with ID $journalPostId was not handled" }
    }
}
