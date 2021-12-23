package no.nav.tpts.mottak.db

import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import javax.sql.DataSource

val LOG = KotlinLogging.logger {}
fun dataSource(): DataSource {
    LOG.info { System.getenv().keys }
    return HikariDataSource().apply {
        dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
        password = System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_PASSWORD")
        username = System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_USERNAME")
        addDataSourceProperty("databaseName", System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_DATABASE"))
//        addDataSourceProperty("password", System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_PASSWORD"))
//        addDataSourceProperty("user", System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_USERNAME"))
    }
}
