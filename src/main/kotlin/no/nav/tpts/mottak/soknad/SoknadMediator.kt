package no.nav.tpts.mottak.soknad

import kotlinx.serialization.decodeFromString
import mu.KotlinLogging
import no.nav.tpts.mottak.clients.saf.SafClient
import no.nav.tpts.mottak.db.queries.PersonQueries
import no.nav.tpts.mottak.joark.models.JoarkSoknad
import no.nav.tpts.mottak.soknad.soknadList.Soknad
import no.nav.tpts.mottak.soknad.soknadList.SoknadQueries
import no.nav.tpts.mottak.soknad.soknadList.insertSoknad
import no.nav.tpts.mottak.soknad.soknadList.lenientJson

val LOG = KotlinLogging.logger {}

suspend fun handleSoknad(journalPostId: String) {
    LOG.info { "Retrieving journalpost metadata with journalPostId $journalPostId" }
    val journalfortDokumentMetaData = SafClient.hentMetadataForJournalpost(journalPostId)

    if (journalfortDokumentMetaData != null) {
        LOG.info { "Retrieving søknad with dokumentInfoId ${journalfortDokumentMetaData.dokumentInfoId}" }
        val jsonSoknad = SafClient.hentSoknad(journalfortDokumentMetaData)
        LOG.info { "Retrieved søknad $jsonSoknad" }

        val joarkSoknad: JoarkSoknad = lenientJson.decodeFromString(jsonSoknad)
        val soknad = Soknad.fromJoarkSoknad(joarkSoknad)

        PersonQueries.insertIfNotExists(soknad.ident, soknad.fornavn, soknad.etternavn)

        LOG.debug { "Saving soknad to database" }
        // lagre soknad to database
        SoknadQueries.insertSoknad(journalPostId.toInt(), journalfortDokumentMetaData.dokumentInfoId?.toInt(), soknad)
    } else {
        LOG.info { "Journalpost with ID $journalPostId was not handled" }
    }
}
