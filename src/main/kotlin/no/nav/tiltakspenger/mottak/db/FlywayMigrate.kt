package no.nav.tiltakspenger.mottak.db

import org.flywaydb.core.Flyway

fun flywayMigrate() {
    Flyway.configure()
        .dataSource(DataSource.hikariDataSource)
        .load()
        .migrate()
}
