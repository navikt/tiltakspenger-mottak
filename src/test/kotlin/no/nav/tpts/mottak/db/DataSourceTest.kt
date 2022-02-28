package no.nav.tpts.mottak.db

import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class DataSourceTest {

    companion object {
        @Container
        @JvmField
        val postgreSQLContainer: TestPostgresqlContainer = TestPostgresqlContainer.instance
    }

    @Test
    fun `flyway migrations skal kjøre uten feil`() {
        flywayMigrate()
    }
}
