package me.tomasan7.jecnamobile.util

import me.tomasan7.jecnaapi.data.grade.Grade
import me.tomasan7.jecnamobile.ui.theme.grade_0
import me.tomasan7.jecnamobile.ui.theme.grade_1
import me.tomasan7.jecnamobile.ui.theme.grade_2
import me.tomasan7.jecnamobile.ui.theme.grade_3
import me.tomasan7.jecnamobile.ui.theme.grade_4
import me.tomasan7.jecnamobile.ui.theme.grade_5

fun getGradeColor(gradeValue: Int) = when (gradeValue)
{
    0    -> grade_0
    1    -> grade_1
    2    -> grade_2
    3    -> grade_3
    4    -> grade_4
    5    -> grade_5
    else -> throw IllegalArgumentException("Grade value must be between 0 and 5. (got $gradeValue)")
}

fun getGradeColor(grade: Grade) = getGradeColor(grade.value)