package no.nav.tiltakspenger.mottak.health

import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class HealthRoutesTest {

    @Test
    fun `empty health checks returns status ok`() {
        testApplication {
            routing {
                healthRoutes(emptyList())
            }
            val response = client.get("/isalive")
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun `one happy health check returns ok`() {
        val healthCheck = object : HealthCheck {
            override fun status(): HealthStatus = HealthStatus.TILFREDS
        }
        testApplication {
            routing {
                healthRoutes(listOf(healthCheck))
            }
            val response = client.get("/isalive")
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun `one unhappy health check returns not ok`() {
        val healthCheck = object : HealthCheck {
            override fun status(): HealthStatus = HealthStatus.ULYKKELIG
        }
        testApplication {
            routing {
                healthRoutes(listOf(healthCheck))
            }
            val client = createClient {
                expectSuccess = false
            }
            val response = client.get("/isalive")
            assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        }
    }
}
