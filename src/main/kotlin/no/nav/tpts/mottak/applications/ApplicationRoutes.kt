package no.nav.tpts.mottak.applications

import io.ktor.application.*
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.principal
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import io.ktor.util.toMap
import no.nav.tpts.mottak.LOG
import no.nav.tpts.mottak.clients.AzureOauthClient
import no.nav.tpts.mottak.clients.saf.SafClient

val JWTPrincipal.userId: String
    get() = this.subject ?: throw NoUserFound("No user subject claim found on token")

class NoUserFound(override val message: String?) : SecurityException(message)

fun Route.applicationRoutes() {
    route("/api/test") {
        get {
            call.respondText("OK")
            LOG.info("Test endpoint")
            val headers = call.request.headers.toMap()
            LOG.info(headers.toString())
            LOG.info("Auth: ${headers["Authorization"]}")

            kotlin.runCatching {
                AzureOauthClient.getToken()
            }.onFailure { e ->
                e.printStackTrace()
            }
        }
    }

    route("/api/soknad/{journalPostId}") {
        post {
            val journalpostId = requireParam("journalpostId")
            val response = SafClient.hentMetadataForJournalpost(journalpostId = journalpostId)
            call.respond(response)
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
                    LOG.info(principal.userId)
                }
            }

            route("/api/onbehalfoftoken") {
                get {
                    /*
                try {
                    val token = AzureOauthClient.getToken()
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respondText("Invalid token", ContentType.Text.Plain, HttpStatusCode.BadRequest)
                    return@get
                }
                call.respondText("OK")
                */
                }
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


