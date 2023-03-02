package me.tomasan7.jecnamobile.settings

import kotlinx.serialization.Serializable
import me.tomasan7.jecnamobile.destinations.TimetableSubScreenDestination

@Serializable
data class Settings(
    val theme: Theme = Theme.SYSTEM,
    val openSubScreenRoute: String = TimetableSubScreenDestination.route,
)
{
    enum class Theme
    {
        DARK,
        LIGHT,
        SYSTEM
    }
}