package no.nav.tiltakspenger.mottak.auth

import java.time.LocalDateTime

class TokenCache {
    var token: String? = null
        private set
    private var expires: LocalDateTime? = null

    internal fun isExpired(): Boolean = expires?.isBefore(LocalDateTime.now()) ?: true

    internal fun update(accessToken: String, expiresIn: Long) {
        token = accessToken
        expires = LocalDateTime.now().plusSeconds(expiresIn)
    }
}
