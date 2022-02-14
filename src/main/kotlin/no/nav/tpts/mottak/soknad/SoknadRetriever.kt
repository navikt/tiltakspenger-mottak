package no.nav.tpts.mottak.soknad

import mu.KotlinLogging
import no.nav.tpts.mottak.clients.saf.SafClient
import no.nav.tpts.mottak.joark.models.JoarkSoknad

val LOG = KotlinLogging.logger {}

suspend fun handleSoknad(journalPostId: String): String {
    LOG.debug { "Retrieving søknad metadat with journalPostId $journalPostId" }
    val journalfortDokumentMetaData = SafClient.hentMetadataForJournalpost(journalPostId)

    LOG.debug { "Retrieving søknad with journalPostId $journalPostId" }
    val joarkSoknad = SafClient.hentSoknad(journalfortDokumentMetaData)

    LOG.debug { "Saving soknad to database" }
    // lagre soknad to database
    return joarkSoknad
}
