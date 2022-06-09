package me.tomasan7.jecnamobile.subscreen.viewmodel

import me.tomasan7.jecnaapi.data.Grade

data class GradesSubScreenState(val loading: Boolean = true, val subjects: List<Subject> = emptyList())

data class Subject(val name: String, val grades: List<Grade>)