package no.nav.tpts.mottak.db

import com.zaxxer.hikari.HikariDataSource
import org.postgresql.ds.PGSimpleDataSource

object DataSource {
    val dataSource = HikariDataSource().apply {
        dataSource = PGSimpleDataSource().apply {
            databaseName = System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_DATABASE")
        }
        password = System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_PASSWORD")
        username = System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_USERNAME")
    }
}
