package no.nav.tiltakspenger.mottak.db

import com.zaxxer.hikari.HikariDataSource
import kotliquery.Connection
import kotliquery.Session
import mu.KotlinLogging

private val LOG = KotlinLogging.logger {}

object DataSource {
    private const val MAX_POOLS = 10
    const val DB_USERNAME_KEY = "DB_USERNAME"
    const val DB_PASSWORD_KEY = "DB_PASSWORD"
    const val DB_DATABASE_KEY = "DB_DATABASE"
    const val DB_HOST_KEY = "DB_HOST"
    const val DB_PORT_KEY = "DB_PORT"

    private fun getEnvOrProp(key: String) = System.getenv(key) ?: System.getProperty(key)

    private fun init(): HikariDataSource {
        LOG.info {
            "Kobler til Postgres '${getEnvOrProp(DB_USERNAME_KEY)}:xxx@" +
                    "${getEnvOrProp(DB_HOST_KEY)}:${getEnvOrProp(DB_PORT_KEY)}/${getEnvOrProp(DB_DATABASE_KEY)}'"
        }

        return HikariDataSource().apply {
            dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
            addDataSourceProperty("serverName", getEnvOrProp(DB_HOST_KEY))
            addDataSourceProperty("portNumber", getEnvOrProp(DB_PORT_KEY))
            addDataSourceProperty("databaseName", getEnvOrProp(DB_DATABASE_KEY))
            addDataSourceProperty("user", getEnvOrProp(DB_USERNAME_KEY))
            addDataSourceProperty("password", getEnvOrProp(DB_PASSWORD_KEY))
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
