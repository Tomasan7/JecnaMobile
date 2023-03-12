package me.tomasan7.jecnamobile.canteen

import kotlinx.serialization.Serializable

@Serializable
data class DishMatchResult(
    val dish: DishDto,
    val compareResult: CompareResult
)