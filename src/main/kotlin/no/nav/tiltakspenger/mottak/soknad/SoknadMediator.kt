package no.nav.tiltakspenger.mottak.soknad

import mu.KotlinLogging
import no.nav.tiltakspenger.mottak.clients.saf.SafClient
import no.nav.tiltakspenger.mottak.db.queries.PersonQueries
import no.nav.tiltakspenger.mottak.soknad.soknadList.Soknad

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
        val dokumentInfoId = journalfortDokumentMetaData.dokumentInfoId?.toInt()
            ?: throw IllegalStateException("Missing dokumentInfoId for søknad")
        SoknadQueries.insertSoknad(
            journalPostId.toInt(),
            dokumentInfoId,
            json,
            soknad
        )
        // Can not insert before soknad is exist
        soknad.barnetillegg.map {
            BarnetilleggQueries.insertBarnetillegg(
                barnetillegg = it,
                journalPostId = journalPostId.toInt(),
                dokumentInfoId = dokumentInfoId
            )
        }
    } else {
        LOG.info { "Journalpost with ID $journalPostId was not handled" }
    }
}
