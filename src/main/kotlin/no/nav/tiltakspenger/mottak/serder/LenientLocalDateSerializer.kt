package no.nav.tiltakspenger.mottak.serder

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.format.DateTimeParseException

object LenientLocalDateSerializer : KSerializer<LocalDate?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDate? {
        return try {
            StrictLocalDateSerializer.deserialize(decoder)
        } catch (ex: DateTimeParseException) {
            null
        }
    }

    override fun serialize(encoder: Encoder, value: LocalDate?) {
        // Denne trenger vi ikke, men må overrides siden vi trenger `deserialize`
    }
}
