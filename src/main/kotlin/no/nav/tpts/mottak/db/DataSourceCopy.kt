package no.nav.tpts.mottak.db

import com.zaxxer.hikari.HikariDataSource
import kotliquery.Connection
import kotliquery.Session
import mu.KotlinLogging
import org.postgresql.ds.PGSimpleDataSource

private val LOG = KotlinLogging.logger {}

class DataSourceCopy {

    companion object {

        private fun init(): HikariDataSource {
            LOG.info { "Setting up datasource with ${System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_DATABASE")}/${System.getProperty("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_DATABASE")}" }
            return HikariDataSource().apply {
                //dataSource = PGSimpleDataSource().apply {
                //    databaseName = dbName().also { LOG.info { it } }
                //}
                jdbcUrl = (System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_URL") ?: System.getProperty("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_URL"))
                password = (System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_PASSWORD") ?: System.getProperty("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_PASSWORD"))
                username = (System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_USERNAME") ?: System.getProperty("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_USERNAME"))
                maximumPoolSize = MAX_POOLS
            }
        }

        private fun dbName() =
            System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_DATABASE") ?: System.getProperty("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_DATABASE")

        private val DATASOURCE: HikariDataSource = init()

        val session: Session by lazy {
            Session(Connection(hikariDataSource.connection))
        }

        val hikariDataSource: HikariDataSource
            get() {
                return DATASOURCE
            }
    }
}
