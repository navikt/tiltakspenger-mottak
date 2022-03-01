package no.nav.tpts.mottak.clients.arena

import io.ktor.application.ApplicationCall
import io.ktor.client.features.get
import io.ktor.client.request.get
import io.ktor.util.pipeline.PipelineContext
import no.nav.tpts.arena.api.ArenaControllerApi
import no.nav.tpts.arena.model.YtelseSak
import no.nav.tpts.mottak.Scope
import no.nav.tpts.mottak.clients.AzureOauthClient
import no.nav.tpts.mottak.common.http.getCallToken

val tptsArenaBaseUrl: String = System.getenv("TPTS_ARENA_URL")

object ArenaClient {
    private val api = ArenaControllerApi(baseUrl = tptsArenaBaseUrl)

    private suspend fun getYtelser(fnr: String, accessToken: String): List<YtelseSak> {
        val oboToken = AzureOauthClient.onBehalfOfExchange(accessToken, Scope.ARENA).accessToken
        api.setBearerToken(oboToken)
        return api.getYtelser(fnr, null, null).body()
    }

    suspend fun PipelineContext<Unit, ApplicationCall>.getYtelser(fnr: String) =
        ArenaClient.getYtelser(fnr, getCallToken())
}
