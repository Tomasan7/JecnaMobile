package me.tomasan7.jecnamobile.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.tomasan7.jecnaapi.repository.AttendancesRepository
import me.tomasan7.jecnaapi.repository.GradesRepository
import me.tomasan7.jecnaapi.repository.WebAttendancesRepository
import me.tomasan7.jecnaapi.repository.WebGradesRepository
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
}