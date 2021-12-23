package no.nav.tpts.mottak.db

import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

fun dataSource(): DataSource {
    return HikariDataSource().apply {
        dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
        password = System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_PASSWORD")
        username = System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_USERNAME")
    }
}
