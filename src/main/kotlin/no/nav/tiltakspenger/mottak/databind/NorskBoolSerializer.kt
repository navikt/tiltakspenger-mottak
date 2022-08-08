package no.nav.tiltakspenger.mottak.databind

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import no.nav.tiltakspenger.mottak.joark.models.NorskBool

object NorskBoolSerializer : KSerializer<NorskBool> {
    override fun deserialize(decoder: Decoder): NorskBool {
        val decoded = decoder.decodeString()
        require(decoded == "nei" || decoded == "ja") { "Failed to deserilaize field, expected 'ja' or 'nei'" }
        return when (decoded) {
            "ja" -> NorskBool.Ja
            else -> NorskBool.Nei
        }
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NorskBoolSerializer", PrimitiveKind.BOOLEAN)
    override fun serialize(encoder: Encoder, value: NorskBool) {
        when (value) {
            NorskBool.Ja -> encoder.encodeBoolean(true)
            else -> encoder.encodeBoolean(false)
        }
    }
}
