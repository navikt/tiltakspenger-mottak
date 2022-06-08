package no.nav.tiltakspenger.mottak.applications

import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.util.toMap
import mu.KotlinLogging
import no.nav.tiltakspenger.mottak.clients.AzureOauthClient

private val LOG = KotlinLogging.logger {}

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
                /*    try {
                        val token = AzureOauthClient.getToken()
                    } catch (e: AuthenticationException) {
                        val log = KotlinLogging.logger {}
                        log.error(e) { e.message }
                        call.respondText("Invalid token", ContentType.Text.Plain, HttpStatusCode.BadRequest)
                        return@get
                    }
                    call.respondText("OK")  */
            }
        }
    }
}
