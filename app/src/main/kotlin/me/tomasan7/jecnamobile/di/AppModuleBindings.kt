package me.tomasan7.jecnamobile.di

import dagger.Binds
import dagger.Module
import dagger.hilt.migration.DisableInstallInCheck
import me.tomasan7.jecnamobile.repository.AuthRepository
import me.tomasan7.jecnamobile.repository.SharedPreferencesAuthRepository
import javax.inject.Singleton

@DisableInstallInCheck
@Module
interface AppModuleBindings
{
    @Binds
    @Singleton
    fun bindAuthRepository(authRepository: SharedPreferencesAuthRepository): AuthRepository
}