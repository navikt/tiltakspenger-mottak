package no.nav.tpts.mottak.db

import com.zaxxer.hikari.HikariDataSource
import kotliquery.Connection
import kotliquery.Session
import mu.KotlinLogging
import org.postgresql.ds.PGSimpleDataSource

const val MAX_POOLS = 10

private val LOG = KotlinLogging.logger {}

object DataSource {
    private fun init(): HikariDataSource {
        LOG.info { "Kobler til Postgres med URL ${System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_URL") ?: System.getProperty("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_URL")} " }
        return HikariDataSource().apply {
            //dataSource = PGSimpleDataSource().apply {
            //    databaseName = System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_DATABASE")
            //}
            jdbcUrl = System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_URL") ?: System.getProperty("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_URL")
            password = System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_PASSWORD")?: System.getProperty("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_PASSWORD")
            username = System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_USERNAME") ?: System.getProperty("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_USERNAME")
            maximumPoolSize = MAX_POOLS
        }
    }

    val hikariDataSource: HikariDataSource by lazy {
        init()
    }

    val session: Session by lazy {
        Session(Connection(hikariDataSource.connection))
    }
}
