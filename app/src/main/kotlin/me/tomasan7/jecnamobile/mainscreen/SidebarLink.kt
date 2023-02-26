package me.tomasan7.jecnamobile.mainscreen

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.ui.graphics.vector.ImageVector
import me.tomasan7.jecnaapi.web.jecna.JecnaWebClient
import me.tomasan7.jecnamobile.R

enum class SidebarLink(
    val link: String,
    @StringRes
    val label: Int,
    val icon: ImageVector
)
{
    SubstitutionTimetable(
        JecnaWebClient.getUrlForPath("/suplovani"), R.string.sidebar_link_substitution_timetable,
        Icons.Outlined.TableChart
    )
}