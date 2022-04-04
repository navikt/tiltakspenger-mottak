package no.nav.tpts.mottak.health

import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class HealthRoutesTest {

    @Test
    fun `empty health checks returns status ok`() {
        testApplication {
            routing {
                healthRoutes(emptyList())
            }
            val response = client.get("/isAlive")
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
            val response = client.get("/isAlive")
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Disabled
    @Test
    fun `one unhappy health check returns not ok`() {
        val healthCheck = object : HealthCheck {
            override fun status(): HealthStatus = HealthStatus.ULYKKELIG
        }
        testApplication {
            routing {
                healthRoutes(listOf(healthCheck))
            }
            val response = client.get("/isAlive")
            assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        }
    }
}
