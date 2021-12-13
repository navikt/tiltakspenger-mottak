package no.nav.tpts.mottak.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.postgresql.ds.PGSimpleDataSource

fun migrate() {
    Flyway.configure()
        .dataSource(configureDatabase())
        .load()
        .migrate()
}

fun configureDatabase(): HikariDataSource {
    return HikariConfig()
        .apply {
            dataSource = PGSimpleDataSource().apply {
                databaseName = "postgres"
            }
            password = System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_PASS")
            username = System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_USER")
        }
        .let { HikariDataSource(it) }
}