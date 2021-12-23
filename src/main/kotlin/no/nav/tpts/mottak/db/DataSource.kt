package no.nav.tpts.mottak.db

import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import javax.sql.DataSource

val LOG = KotlinLogging.logger {}
fun dataSource(): DataSource {
    return HikariDataSource().apply {
        dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
        password = System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_PASS")
        username = System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_USER")
    }
}
