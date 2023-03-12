@file:UseSerializers(IntRangeSerializer::class)

package me.tomasan7.jecnamobile.canteen

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import me.tomasan7.canteenserver.serialization.IntRangeSerializer

@Serializable
data class CompareResult(
    val source: String,
    val target: String,
    val score: Float,
    /** On the [source] string. */
    val matchPart: IntRange
)