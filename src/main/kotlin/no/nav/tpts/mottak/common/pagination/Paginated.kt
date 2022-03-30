package no.nav.tpts.mottak.common.pagination

import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.Serializable

const val DEFAULT_PAGE_SIZE = 20

@Serializable
class Paginated<T>(
    val data: List<T>,
    val total: Int,
    val offset: Int,
    val pageSize: Int
)

data class PageData<T>(val data: List<T>, val total: Int)

suspend inline fun <reified T> PipelineContext<Unit, ApplicationCall>.paginate(
    block: (offset: Int, pageSize: Int) -> PageData<T>
) {
    val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
    val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: DEFAULT_PAGE_SIZE
    val pageData = block(offset, pageSize)
    call.respond(
        Paginated(
            total = pageData.total,
            data = pageData.data,
            offset = offset,
            pageSize = pageSize
        )
    )
}
