package no.nav.tpts.mottak.health

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.routing
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class HealthRoutesTest {

    @Test
    fun `empty health checks returns status ok`() {
        withTestApplication({ routing { healthRoutes(emptyList()) } }) {
            handleRequest(method = HttpMethod.Get, uri = "/isAlive").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun `one happy health check returns ok`() {
        val healthCheck = object : HealthCheck {
            override fun status(): HealthStatus = HealthStatus.TILFREDS
        }
        withTestApplication({ routing { healthRoutes(listOf(healthCheck)) } }) {
            handleRequest(method = HttpMethod.Get, uri = "/isAlive").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun `one unhappy health check returns not ok`() {
        val healthCheck = object : HealthCheck {
            override fun status(): HealthStatus = HealthStatus.ULYKKELIG
        }
        withTestApplication({ routing { healthRoutes(listOf(healthCheck)) } }) {
            handleRequest(method = HttpMethod.Get, uri = "/isAlive").apply {
                assertEquals(HttpStatusCode.ServiceUnavailable, response.status())
            }
        }
    }
}
