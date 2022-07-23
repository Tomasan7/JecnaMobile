package me.tomasan7.jecnamobile.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.tomasan7.jecnaapi.repository.*
import me.tomasan7.jecnaapi.web.JecnaWebClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object AppModule
{
    @Provides
    @Singleton
    fun provideJecnaWebClient() = JecnaWebClient()

    @Provides
    @Singleton
    fun provideGradesRepository(jecnaWebClient: JecnaWebClient): GradesRepository = WebGradesRepository(jecnaWebClient)

    @Provides
    @Singleton
    fun provideAttendancesRepository(jecnaWebClient: JecnaWebClient): AttendancesRepository = WebAttendancesRepository(jecnaWebClient)

    @Provides
    @Singleton
    fun provideTimetableRepository(jecnaWebClient: JecnaWebClient): TimetableRepository = WebTimetableRepository(jecnaWebClient)
}