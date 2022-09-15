package me.tomasan7.jecnamobile.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.tomasan7.jecnaapi.repository.*
import me.tomasan7.jecnaapi.web.JecnaWebClient
import me.tomasan7.jecnamobile.repository.AuthRepository
import me.tomasan7.jecnamobile.repository.SharedPreferencesAuthRepository
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
    fun provideAuthRepository(@ApplicationContext appContext: Context): AuthRepository = SharedPreferencesAuthRepository(appContext)

    @Provides
    @Singleton
    fun provideGradesRepository(jecnaWebClient: JecnaWebClient): GradesRepository = WebGradesRepository(jecnaWebClient)

    @Provides
    @Singleton
    fun provideAttendancesRepository(jecnaWebClient: JecnaWebClient): AttendancesRepository = WebAttendancesRepository(jecnaWebClient)

    @Provides
    @Singleton
    fun provideTimetableRepository(jecnaWebClient: JecnaWebClient): TimetableRepository = WebTimetableRepository(jecnaWebClient)

    @Provides
    @Singleton
    fun provideArticlesRepository(jecnaWebClient: JecnaWebClient): ArticlesRepository = WebArticlesRepository(jecnaWebClient)
}