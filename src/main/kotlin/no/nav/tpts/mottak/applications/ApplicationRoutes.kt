package no.nav.tpts.mottak.applications

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.principal
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.routing.get
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.toMap
import no.nav.tpts.mottak.LOG
import no.nav.tpts.mottak.clients.AzureOauthClient
import no.nav.tpts.mottak.models.toSoknad
import no.nav.tpts.mottak.saf.api.SAFClient
import java.lang.IllegalArgumentException

val JWTPrincipal.userId: String
    get() = this.subject ?: throw Exception("No user subject claim found on token")

fun Route.applicationRoutes() {
    route("/api/test") {
        get {
            call.respondText("OK")
            LOG.info("Test endpoint")
            val headers = call.request.headers.toMap()
            LOG.info(headers.toString())
            LOG.info("Auth: ${headers["Authorization"]}")

            try {
                AzureOauthClient.getToken()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    route("/api/mocksoknad") {
        get {
            call.respondText(
                text = javaClass.getResource("/mocksoknadList.json")?.readText(Charsets.UTF_8) ?: "{}",
                contentType = ContentType.Application.Json
            )
        }
    }.also { LOG.info { "setting up endpoint /api/mocksoknad" } }

    route("/api/mocksoknad/{id}") {
        get {
            val soknadId = call.parameters["id"]
            print(call.parameters.entries())
            print(call.parameters["id"])
            call.respondText(
                text = javaClass.getResource("/mocksoknad${soknadId ?: ""}.json")?.readText(Charsets.UTF_8) ?: "{}",
                contentType = ContentType.Application.Json
            )
        }
    }.also { LOG.info { "setting up endpoint /api/mocksoknad/{id}" } }

    authenticate("auth-jwt") {
        route("/api/application") {
            get {
                call.respondText("OK")
                val principal = call.principal<JWTPrincipal>()
                LOG.info(principal!!.payload.claims.toString())
                LOG.info(principal!!.userId)
            }
        }

        route("/api/onbehalfoftoken") {
            get {
                try {
                    val token = AzureOauthClient.getToken()
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respondText("Invalid token", ContentType.Text.Plain, HttpStatusCode.BadRequest)
                    return@get
                }
                call.respondText("OK")
            }
        }

        route("/api/soknad/{journalPostId}") {
            get {
                val dokumentInfoId = requireQueryParam("dokumentInfoId")
                val journalPostId =  requireParam("journalPostId")
                val response = SAFClient.getJournalPostDocument(journalpostId = journalPostId, dokumentInfoId = dokumentInfoId)
                call.respond(response.toSoknad())
            }
        }
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.requireParam(paramName: String): String {
    val param = call.parameters[paramName]
    if (param == null) {
        call.respond(HttpStatusCode.BadRequest, "Missing path param ${paramName}")
        throw IllegalArgumentException("Missing path param ${paramName}")
    }
    return param
}
suspend fun PipelineContext<Unit, ApplicationCall>.requireQueryParam(paramName: String): String {
    val param = call.request.queryParameters[paramName]
    if (param == null) {
        call.respond(HttpStatusCode.BadRequest, "Missing path param ${paramName}")
        throw IllegalArgumentException("Missing path param ${paramName}")
    }
    return param
}