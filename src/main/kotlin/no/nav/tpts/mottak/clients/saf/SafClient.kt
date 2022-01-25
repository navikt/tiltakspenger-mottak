package no.nav.tpts.mottak.clients.saf

interface SafClient {
    fun hentMetadataForJournalpost(journalpostId: String): JournalfortDokumentMetaData
}

data class JournalfortDokumentMetaData(
    val journalpostId: String,
    val dokumentInfoId: String,
    val dokumentTittel: String,
)
