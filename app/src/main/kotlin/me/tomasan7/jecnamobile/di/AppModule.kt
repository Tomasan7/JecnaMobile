package me.tomasan7.jecnamobile.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.tomasan7.jecnaapi.parser.parsers.HtmlAttendancesPageParserImpl
import me.tomasan7.jecnaapi.parser.parsers.HtmlGradesPageParserImpl
import me.tomasan7.jecnaapi.parser.parsers.HtmlNewsPageParserImpl
import me.tomasan7.jecnaapi.parser.parsers.HtmlTimetableParserImpl
import me.tomasan7.jecnaapi.repository.*
import me.tomasan7.jecnaapi.web.JecnaWebClient
import javax.inject.Singleton

@Module(includes = [AppModuleBindings::class])
@InstallIn(SingletonComponent::class)
internal object AppModule
{
    @Provides
    @Singleton
    fun provideJecnaWebClient() = JecnaWebClient(autoLogin = true)

    @Provides
    @Singleton
    fun provideGradesRepository(jecnaWebClient: JecnaWebClient): GradesRepository = WebGradesRepository(jecnaWebClient, HtmlGradesPageParserImpl)

    @Provides
    @Singleton
    fun provideAttendancesRepository(jecnaWebClient: JecnaWebClient): AttendancesRepository = WebAttendancesRepository(jecnaWebClient, HtmlAttendancesPageParserImpl)

    @Provides
    @Singleton
    fun provideTimetableRepository(jecnaWebClient: JecnaWebClient): TimetableRepository = WebTimetableRepository(jecnaWebClient, HtmlTimetableParserImpl)

    @Provides
    @Singleton
    fun provideNewsRepository(jecnaWebClient: JecnaWebClient): NewsRepository = WebNewsRepository(jecnaWebClient, HtmlNewsPageParserImpl)
}