package no.nav.tpts.mottak.db

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy

class TestPostgresqlContainer private constructor() : PostgreSQLContainer<TestPostgresqlContainer?>(IMAGE_VERSION) {

    companion object {
        private const val IMAGE_VERSION = "postgres:14.2"

        private val CONTAINER: TestPostgresqlContainer = TestPostgresqlContainer().waitingFor(HostPortWaitStrategy())!!

        val instance: TestPostgresqlContainer
            get() {
                return CONTAINER
            }
    }

    override fun start() {
        super.start()
        System.setProperty("DB_URL", CONTAINER.jdbcUrl)
        // Dette er ikke nok, trenger å sette hostname og port også hvis man skal bruke databaseName:
        // System.setProperty("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_DATABASE", CONTAINER.databaseName)
        System.setProperty("DB_USERNAME", CONTAINER.username)
        System.setProperty("DB_PASSWORD", CONTAINER.password)
    }

    override fun stop() {
        // do nothing, JVM handles shut down
    }
}
