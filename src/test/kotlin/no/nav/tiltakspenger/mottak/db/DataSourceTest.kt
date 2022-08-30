package no.nav.tiltakspenger.mottak.db

import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class DataSourceTest {

    companion object {
        @Container
        val postgreSQLContainer = PostgresTestcontainer
    }

    @Test
    fun `flyway migrations skal kjøre uten feil`() {
        flywayMigrate()
    }
}
