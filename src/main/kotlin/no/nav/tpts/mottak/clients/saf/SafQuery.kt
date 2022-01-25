package no.nav.tpts.mottak.clients.saf

object SafQuery {

    object journalpost {
        val query = """
            query(${"$"}journalpostId: String!){
                journalpost(journalpostId: ${"$"}journalpostId){
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

        data class Variables(
            val journalpostId: String
        )

        data class Response(
            override val errors: List<Graphql.GraphqlError>?,
            override val data: ResponseData?
        ) : Graphql.GraphqlResponse<ResponseData>

        data class ResponseData(
            val journalpost: Journalpost
        )

        data class Journalpost(
            val journalpostId: String,
            val dokumenter: List<DokumentInfo> = arrayListOf()
        )

        data class DokumentInfo (
            val dokumentInfoId: String,
            val tittel: String,
            val dokumentvarianter: List<Dokumentvariant> = arrayListOf()
        )

        data class Dokumentvariant (
            val variantformat: Variantformat,
            val filnavn: String,
            val filtype: String
        )

        enum class Variantformat {
            ARKIV,
            ORIGINAL
        }
    }
}