package no.nav.tpts.mottak.db

import com.zaxxer.hikari.HikariDataSource
import kotliquery.Connection
import kotliquery.Session
import org.postgresql.ds.PGSimpleDataSource

object DataSource {
    private const val MAX_POOLS = 10
    private fun getEnvOrProp(name: String) = System.getenv(name) ?: System.getProperty(name)
    private fun init(): HikariDataSource {
        return HikariDataSource().apply {
            dataSource = PGSimpleDataSource().apply {
                databaseName = getEnvOrProp("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_DATABASE")
            }
            password = getEnvOrProp("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_PASSWORD")
            username = getEnvOrProp("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_USERNAME")
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
