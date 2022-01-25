package no.nav.tpts.mottak.common.http

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

suspend fun ApplicationCall.badRequest(message: String) {
    this.respond(HttpStatusCode.BadRequest, message)
}

suspend fun ApplicationCall.notFound(message: String) {
    this.respond(HttpStatusCode.NotFound, message)
}
