package no.nav.tpts.mottak.soknad

import mu.KotlinLogging
import no.nav.tpts.mottak.clients.saf.SafClient
import no.nav.tpts.mottak.db.queries.PersonQueries
import no.nav.tpts.mottak.soknad.soknadList.Soknad

private val LOG = KotlinLogging.logger {}

suspend fun handleSoknad(journalPostId: String) {
    LOG.info { "Retrieving journalpost metadata with journalPostId $journalPostId" }
    val journalfortDokumentMetaData = SafClient.hentMetadataForJournalpost(journalPostId)

    if (journalfortDokumentMetaData != null) {
        LOG.info { "Retrieving søknad with dokumentInfoId ${journalfortDokumentMetaData.dokumentInfoId}" }
        val json = SafClient.hentSoknad(journalfortDokumentMetaData)
        LOG.info { "Retrieved søknad with dokumentInfoId ${journalfortDokumentMetaData.dokumentInfoId}" }
        val soknad = Soknad.fromJson(json)
        PersonQueries.insertIfNotExists(soknad.ident, soknad.fornavn, soknad.etternavn)
        LOG.info { "Saving soknad to database with dokumentInfoId ${journalfortDokumentMetaData.dokumentInfoId}" }
        SoknadQueries.insertSoknad(
            journalPostId.toInt(),
            journalfortDokumentMetaData.dokumentInfoId?.toInt(),
            json,
            soknad
        )
    } else {
        LOG.info { "Journalpost with ID $journalPostId was not handled" }
    }
}
