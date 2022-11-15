package no.nav.tiltakspenger.mottak.saf

import kotlinx.serialization.Serializable

object SafQuery {

    @Serializable
    data class Response(
        override val errors: List<GraphqlError>? = null,
        override val data: ResponseData? = null
    ) : GraphqlResponse<ResponseData>

    @Serializable
    data class ResponseData(
        val journalpost: Journalpost
    )

    @Serializable
    data class Journalpost(
        val journalpostId: String,
        val dokumenter: List<DokumentInfo> = arrayListOf()
    )

    @Serializable
    data class DokumentInfo(
        val dokumentInfoId: String,
        val tittel: String?,
        val dokumentvarianter: List<Dokumentvariant> = arrayListOf()
    )

    @Serializable
    data class Dokumentvariant(
        val variantformat: Variantformat,
        val filnavn: String?,
        val filtype: String
    )

    enum class Variantformat {
        ARKIV,
        ORIGINAL
    }
}

fun journalpost(journalpostId: String): String {

    return """
        query{
            journalpost(journalpostId: "$journalpostId"){
                journalpostId
                dokumenter {
                    dokumentInfoId
                    tittel
                    dokumentvarianter {
                        variantformat
                        filnavn
                        filtype
                    }
                }
            }
        }
    """.trimIndent()
}

data class JournalfortDokumentMetadata(
    val journalpostId: String,
    val dokumentInfoId: String,
    val filnavn: String?,
    val vedlegg: List<VedleggMetadata> = emptyList(),
)

data class VedleggMetadata(
    val journalpostId: String,
    val dokumentInfoId: String,
    val filnavn: String?,
)
