package no.nav.tpts.mottak.common.http

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpHeaders
import io.ktor.util.pipeline.PipelineContext

suspend fun PipelineContext<Unit, ApplicationCall>.getCallToken(): String {
    return call.request.headers[HttpHeaders.Authorization]?.split("Bearer ")?.lastOrNull()
        ?: throw IllegalAccessException("Not authorized")
}
