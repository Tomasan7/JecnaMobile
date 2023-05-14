package me.tomasan7.canteenserver.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object IntRangeSerializer : KSerializer<IntRange>
{
    override val descriptor = IntRangeSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: IntRange)
    {
        encoder.encodeSerializableValue(IntRangeSurrogate.serializer(), value.toSurrogate())
    }

    override fun deserialize(decoder: Decoder): IntRange
    {
        return decoder.decodeSerializableValue(IntRangeSurrogate.serializer()).toRange()
    }

    @Serializable
    data class IntRangeSurrogate(
        val start: Int,
        val endInclusive: Int
    )

    private fun IntRangeSurrogate.toRange() = IntRange(start, endInclusive)

    private fun IntRange.toSurrogate() = IntRangeSurrogate(start, endInclusive)
}