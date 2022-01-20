package no.nav.tpts.mottak.db

import com.zaxxer.hikari.HikariDataSource
import kotliquery.Connection
import kotliquery.Session
import org.postgresql.ds.PGSimpleDataSource

object DataSource {
    private fun init(): HikariDataSource {
        return HikariDataSource().apply {
            dataSource = PGSimpleDataSource().apply {
                databaseName = System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_DATABASE")
            }
            password = System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_PASSWORD")
            username = System.getenv("NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_USERNAME")
        }
    }

    val hikariDataSource: HikariDataSource by lazy {
        init()
    }

    val session: Session by lazy {
        Session(Connection(hikariDataSource.connection))
    }
}
