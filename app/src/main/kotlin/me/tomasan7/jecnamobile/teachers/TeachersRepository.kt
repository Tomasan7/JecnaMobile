package me.tomasan7.jecnamobile.teachers

import me.tomasan7.jecnaapi.data.schoolStaff.Teacher
import me.tomasan7.jecnaapi.data.schoolStaff.TeacherReference
import me.tomasan7.jecnaapi.data.schoolStaff.TeachersPage

interface TeachersRepository
{
    suspend fun getTeachersPage(): TeachersPage
    suspend fun getTeacher(tag: String): Teacher
    suspend fun getTeacher(ref: TeacherReference): Teacher = getTeacher(ref.tag)
}
