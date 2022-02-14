package no.nav.tpts.mottak.auth

import com.auth0.jwt.interfaces.Payload
import io.ktor.application.ApplicationCall
import io.ktor.auth.jwt.JWTCredential
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ValidateJwtClaimsTest {

    private val validClientId = "123-321-123-321"
    private val validIssuer = "https://login.microsoftonline.com/966ac572-f5b7-4bbe-aa88-c76419c0f851/v2.0"
    val validator = validateJwtClaims(validClientId, validIssuer)

    private fun doTest(success: Boolean, clientId: String) {
        val payload: Payload = mockk()
        val credential: JWTCredential = JWTCredential(
            payload = payload
        )
        every { payload.audience } returns listOf(clientId)
        every { payload.issuer } returns validIssuer

        val mockCall: ApplicationCall = mockk()
        val result = runBlocking {
            validator(mockCall, credential)
        }
        assertEquals(success, result != null)
    }

    @Test
    fun `should accept valid aud and issuer`() {
        doTest(true, validClientId)
    }

    @Test
    fun `should not accept invalid aud and valid issuer`() {
        doTest(false, "123-321-123-322")
    }
}
