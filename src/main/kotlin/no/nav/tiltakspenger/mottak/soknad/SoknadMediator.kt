package no.nav.tiltakspenger.mottak.soknad

import mu.KotlinLogging
import no.nav.tiltakspenger.mottak.clients.saf.SafClient
import no.nav.tiltakspenger.mottak.db.queries.PersonQueries
import no.nav.tiltakspenger.mottak.soknad.soknadList.Soknad

private val LOG = KotlinLogging.logger {}

suspend fun handleSoknad(journalpostId: String) {
    LOG.info { "Retrieving journalpost metadata with journalpostId $journalpostId" }
    val journalfortDokumentMetaData = SafClient.hentMetadataForJournalpost(journalpostId)

    if (journalfortDokumentMetaData != null) {
        LOG.info { "Retrieving søknad with dokumentInfoId ${journalfortDokumentMetaData.dokumentInfoId}" }
        val json = SafClient.hentSoknad(journalfortDokumentMetaData)
        LOG.info { "Retrieved søknad with dokumentInfoId ${journalfortDokumentMetaData.dokumentInfoId}" }
        val soknad = Soknad.fromJson(json)
        PersonQueries.insertIfNotExists(soknad.ident, soknad.fornavn, soknad.etternavn)
        val dokumentInfoId = journalfortDokumentMetaData.dokumentInfoId?.toInt()
            ?: throw IllegalStateException("Missing dokumentInfoId for søknad")
        SoknadQueries.insertIfNotExists(
            journalpostId.toInt(),
            dokumentInfoId,
            json,
            soknad
        )
        // Can not be inserted before soknad exists
        soknad.barnetillegg.map {
            BarnetilleggQueries.insertBarnetillegg(
                barnetillegg = it,
                journalpostId = journalpostId.toInt(),
                dokumentInfoId = dokumentInfoId
            )
        }
    } else {
        LOG.info { "Journalpost with ID $journalpostId was not handled" }
    }
}
