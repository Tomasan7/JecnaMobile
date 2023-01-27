package me.tomasan7.jecnamobile.util

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val theme: Theme = Theme.SYSTEM,
    val openSubScreenRoute: String = TODO("Provide a default value for openSubScreenRoute"),
)

enum class Theme
{
    DARK,
    LIGHT,
    SYSTEM
}