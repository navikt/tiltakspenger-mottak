package no.nav.tpts.mottak.db

import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import javax.sql.DataSource

val LOG = KotlinLogging.logger {}
fun dataSource(): DataSource {
    val env = System.getenv()
    LOG.info { ("env: $env") }
    return HikariDataSource().apply {
        jdbcUrl = "jdbc:${env["NAIS_DATABASE_TPTS_TILTAKSPENGER_MOTTAK_DB_URL"]}"
    }
}
