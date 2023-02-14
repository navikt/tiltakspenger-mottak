package no.nav.tiltakspenger.mottak.auth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class TokenCacheTest {

    @Test
    fun `Should be expired if actually expired`() {
        val tokenCache = TokenCache()
        tokenCache.update(
            accessToken = "token",
            expiresIn = -1,
        )
        assertTrue(tokenCache.isExpired())
    }

    @Test
    fun `Should not be expired initially`() {
        val tokenCache = TokenCache()
        tokenCache.update(
            accessToken = "token",
            expiresIn = 100,
        )
        assertFalse(tokenCache.isExpired())
    }

    @Test
    fun `Should return cached token`() {
        val tokenCache = TokenCache()
        tokenCache.update(
            accessToken = "token",
            expiresIn = 100,
        )
        assertEquals("token", tokenCache.token)
    }
}
