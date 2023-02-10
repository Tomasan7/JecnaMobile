package me.tomasan7.jecnamobile.di

import dagger.Binds
import dagger.Module
import dagger.hilt.migration.DisableInstallInCheck
import me.tomasan7.jecnamobile.grades.GradesRepository
import me.tomasan7.jecnamobile.grades.GradesRepositoryImpl
import me.tomasan7.jecnamobile.login.AuthRepository
import me.tomasan7.jecnamobile.login.SharedPreferencesAuthRepository
import me.tomasan7.jecnamobile.timetable.TimetableRepository
import me.tomasan7.jecnamobile.timetable.TimetableRepositoryImpl
import javax.inject.Singleton

@DisableInstallInCheck
@Module
interface AppModuleBindings
{
    @Binds
    @Singleton
    fun bindAuthRepository(repository: SharedPreferencesAuthRepository): AuthRepository

    @Binds
    @Singleton
    fun bindGradesRepository(repository: GradesRepositoryImpl): GradesRepository

    @Binds
    @Singleton
    fun bindTimetableRepository(repository: TimetableRepositoryImpl): TimetableRepository
}