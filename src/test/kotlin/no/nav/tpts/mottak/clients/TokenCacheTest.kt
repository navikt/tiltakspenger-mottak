package no.nav.tpts.mottak.clients

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TokenCacheTest {

    @Test
    fun `Should be expired if actually expired`() {
        val tokenCache = TokenCache()
        tokenCache.update(
            "token",
            -1
        )
        Assertions.assertEquals(true, tokenCache.isExpired())
    }

    @Test
    fun `Should not be expired initially`() {
        val tokenCache = TokenCache()
        tokenCache.update(
            "token",
            100
        )
        Assertions.assertEquals(false, tokenCache.isExpired())
    }

    @Test
    fun `should return cached token`() {
        val tokenCache = TokenCache()
        tokenCache.update(
            "token",
            100
        )
        Assertions.assertEquals("token", tokenCache.token)
    }
}
