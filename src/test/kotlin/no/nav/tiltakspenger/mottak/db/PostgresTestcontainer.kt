package no.nav.tiltakspenger.mottak.db

import no.nav.tiltakspenger.mottak.db.DataSource.DB_DATABASE_KEY
import no.nav.tiltakspenger.mottak.db.DataSource.DB_HOST_KEY
import no.nav.tiltakspenger.mottak.db.DataSource.DB_PASSWORD_KEY
import no.nav.tiltakspenger.mottak.db.DataSource.DB_PORT_KEY
import no.nav.tiltakspenger.mottak.db.DataSource.DB_USERNAME_KEY
import org.testcontainers.containers.PostgreSQLContainer

object PostgresTestcontainer : PostgreSQLContainer<PostgresTestcontainer?>("postgres:14.4") {

    override fun start() {
        super.start()
        System.setProperty(DB_HOST_KEY, host)
        System.setProperty(DB_PORT_KEY, getMappedPort(POSTGRESQL_PORT).toString())
        System.setProperty(DB_DATABASE_KEY, databaseName)
        System.setProperty(DB_USERNAME_KEY, username)
        System.setProperty(DB_PASSWORD_KEY, password)
    }

    override fun stop() {
        System.clearProperty(DB_HOST_KEY)
        System.clearProperty(DB_PORT_KEY)
        System.clearProperty(DB_DATABASE_KEY)
        System.clearProperty(DB_USERNAME_KEY)
        System.clearProperty(DB_PASSWORD_KEY)
    }
}
