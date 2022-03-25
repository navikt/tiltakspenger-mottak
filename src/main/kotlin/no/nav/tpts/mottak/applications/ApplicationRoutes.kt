package no.nav.tpts.mottak.applications

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.principal
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.util.toMap
import no.nav.tpts.mottak.clients.AzureOauthClient
import org.apache.logging.log4j.kotlin.logger

private val LOG = logger("no.nav.tpts.mottak.applications.ApplicationRoutes")

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
