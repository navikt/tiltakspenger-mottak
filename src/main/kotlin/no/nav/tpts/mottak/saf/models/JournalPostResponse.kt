package no.nav.tpts.mottak.saf.models

import kotlinx.serialization.Serializable

@Serializable
data class DocumentVariant(val variantformat: String, val filtype: String, val filnavn: String)
@Serializable
data class Document(
    val dokumentInfoId: String,
    val tittel: String,
    val brevkode: String,
    val dokumentvarianter: List<DocumentVariant>
)
@Serializable
data class JournalPost(val journalpostId: String, val dokumenter: List<Document>)
@Serializable
data class Data(val journalpost: JournalPost)
@Serializable
data class JournalPostResponse(val data: Data)
