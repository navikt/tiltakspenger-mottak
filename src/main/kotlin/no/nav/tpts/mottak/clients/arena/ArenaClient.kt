package no.nav.tpts.mottak.clients.arena

import io.ktor.application.ApplicationCall
import io.ktor.client.features.get
import io.ktor.client.request.get
import io.ktor.util.pipeline.PipelineContext
import no.nav.tpts.arena.api.ArenaControllerApi
import no.nav.tpts.arena.model.YtelseSak
import no.nav.tpts.mottak.Scope
import no.nav.tpts.mottak.clients.AzureOauthClient
import no.nav.tpts.mottak.clients.TokenCache
import no.nav.tpts.mottak.common.http.getCallToken

val tptsArenaBaseUrl: String = System.getenv("TPTS_ARENA_URL")
fun arenaUrl(fnr: String): String = "$tptsArenaBaseUrl/arena/soap/ytelser/$fnr"

object ArenaClient {
    private val tokenCache = TokenCache()
    private val api = ArenaControllerApi(baseUrl = "url here")

    private suspend fun getArenaToken(token: String): String {
        val currentToken = tokenCache.token
        if (!tokenCache.isExpired() && currentToken != null) return currentToken
        return AzureOauthClient.onBehalfOfExchange(token, Scope.ARENA).accessToken
    }

    private suspend fun getYtelser(fnr: String, accessToken: String): List<YtelseSak> {
        api.setBearerToken(getArenaToken(accessToken))
        return api.getYtelser(fnr, null, null).body()
    }

    suspend fun PipelineContext<Unit, ApplicationCall>.getYtelser(fnr: String) =
        ArenaClient.getYtelser(fnr, getCallToken())
}
