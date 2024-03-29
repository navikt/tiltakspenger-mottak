package no.nav.tiltakspenger.mottak.serder

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDateTime =
        ZonedDateTime.parse(decoder.decodeString()).withZoneSameInstant(ZoneId.of("Europe/Oslo")).toLocalDateTime()

    override fun serialize(encoder: Encoder, value: LocalDateTime) = encoder.encodeString(value.toString())
}
