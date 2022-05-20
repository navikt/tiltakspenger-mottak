package no.nav.tpts.mottak.db

import no.nav.tpts.mottak.db.DataSource.DB_DATABASE_KEY
import no.nav.tpts.mottak.db.DataSource.DB_HOST_KEY
import no.nav.tpts.mottak.db.DataSource.DB_PASSWORD_KEY
import no.nav.tpts.mottak.db.DataSource.DB_PORT_KEY
import no.nav.tpts.mottak.db.DataSource.DB_USERNAME_KEY
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
        System.setProperty(DB_HOST_KEY, CONTAINER.host)
        System.setProperty(DB_PORT_KEY, CONTAINER.getMappedPort(POSTGRESQL_PORT).toString())
        System.setProperty(DB_DATABASE_KEY, CONTAINER.databaseName)
        System.setProperty(DB_USERNAME_KEY, CONTAINER.username)
        System.setProperty(DB_PASSWORD_KEY, CONTAINER.password)
    }

    override fun stop() {
        // do nothing, JVM handles shut down
    }
}
