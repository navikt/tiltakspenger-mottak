package no.nav.tpts.mottak.soknad

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotliquery.Connection
import kotliquery.Row
import kotliquery.Session
import kotliquery.queryOf
import no.nav.tpts.mottak.db.DataSource
import org.intellij.lang.annotations.Language
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

fun Route.soknadRoutes() {
    route("/soknad") {
        get {
            @Language("SQL")
            val query = """
                select navn, opprettet_dato, bruker_start_dato, bruker_slutt_dato from soknad
            """.trimIndent()

            val session = Session(Connection(DataSource.dataSource.connection), strict = true)
            val soknader = session.run(queryOf(query).map(Soknad::fromRow).asList)
            call.respond(soknader)
        }
    }
}

@Serializable
data class Soknad(
    val navn: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val opprettetDato: LocalDateTime,
    @Serializable(with = LocalDateSerializer::class)
    val brukerStartDato: LocalDate?,
    @Serializable(with = LocalDateSerializer::class)
    val brukerSluttDato: LocalDate?
) {
    companion object {
        fun fromRow(row: Row): Soknad {
            return Soknad(
                navn = row.string("navn"),
                opprettetDato = row.zonedDateTime("opprettet_dato").toLocalDateTime(),
                brukerStartDato = row.localDateOrNull("bruker_start_dato"),
                brukerSluttDato = row.localDateOrNull("bruker_slutt_dato")
            )
        }
    }
}

object LocalDateSerializer : KSerializer<LocalDate> {
    override fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString())
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDate) = encoder.encodeString(value.toString())
}

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override fun deserialize(decoder: Decoder) = ZonedDateTime.parse(decoder.decodeString()).toLocalDateTime()
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDateTime) = encoder.encodeString(value.toString())
}
