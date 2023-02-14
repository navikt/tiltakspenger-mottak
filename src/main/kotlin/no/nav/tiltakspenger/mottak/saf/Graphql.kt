package no.nav.tiltakspenger.mottak.saf

import kotlinx.serialization.Serializable

@Serializable
data class Graphql(val query: String)

interface GraphqlResponse<D> {
    val errors: List<GraphqlError>?
    val data: D?
}

@Serializable
data class GraphqlError(
    val message: String? = null,
    val locations: List<GraphqlErrorLocation>? = null,
    val path: List<String>? = null,
    val extensions: GraphqlErrorExtensions? = null,
)

@Serializable
data class GraphqlErrorLocation(
    val line: Int?,
    val column: Int?,
)

@Serializable
data class GraphqlErrorExtensions(
    val code: String? = null,
    val classification: String? = null,
)
