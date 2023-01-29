package me.tomasan7.jecnamobile.util

import kotlinx.serialization.Serializable
import me.tomasan7.jecnamobile.login.destinations.LoginScreenDestination

@Serializable
data class Settings(
    val theme: Theme = Theme.SYSTEM,
    val openSubScreenRoute: String = LoginScreenDestination.route,
)

enum class Theme
{
    DARK,
    LIGHT,
    SYSTEM
}