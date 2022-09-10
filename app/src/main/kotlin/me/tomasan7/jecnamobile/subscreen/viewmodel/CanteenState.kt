package me.tomasan7.jecnamobile.subscreen.viewmodel

import me.tomasan7.jecnaapi.data.canteen.Menu

data class CanteenState(
    val loading: Boolean = false,
    val menu: Menu? = null
)