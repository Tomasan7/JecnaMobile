package me.tomasan7.jecnamobile.canteen

import kotlinx.serialization.Serializable

@Serializable
data class DishDto(
    val id: Int? = null,
    val description: String,
    val imageId: Int? = null
)