package me.tomasan7.jecnamobile.subscreen.viewmodel

import me.tomasan7.jecnaapi.data.grade.GradesPage

data class GradesSubScreenState(val loading: Boolean = true, val gradesPage: GradesPage? = null)