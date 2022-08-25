package no.nav.tiltakspenger.mottak.databind

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object StringBooleanSerializer : KSerializer<Boolean> {
    private val sant = Regex("true|ja", RegexOption.IGNORE_CASE)
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("StringBoolean", PrimitiveKind.STRING)

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder) = decoder.decodeNotNullMark() && decoder.decodeString().matches(sant)

    override fun serialize(encoder: Encoder, value: Boolean) {
        TODO("Not yet implemented")
    }
}
