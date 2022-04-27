package no.nav.tpts.mottak.db

import com.zaxxer.hikari.HikariDataSource
import kotliquery.Connection
import kotliquery.Session
import mu.KotlinLogging

private val LOG = KotlinLogging.logger {}

object DataSource {
    private const val MAX_POOLS = 10
    private fun getEnvOrProp(name: String) = System.getenv(name) ?: System.getProperty(name)
    private fun init(): HikariDataSource {
        LOG.info { "Kobler til Postgres med URL ${getEnvOrProp("DB_URL")}" }
        return HikariDataSource().apply {
//            dataSource = PGSimpleDataSource().apply {
//                databaseName = getEnvOrProp("DB_DATABASE")
//            }
            password = getEnvOrProp("DB_PASSWORD")
            username = getEnvOrProp("DB_USERNAME")
            jdbcUrl = getEnvOrProp("DB_URL")
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
