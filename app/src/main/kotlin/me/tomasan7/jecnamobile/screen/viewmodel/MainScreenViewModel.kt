package me.tomasan7.jecnamobile.screen.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import me.tomasan7.jecnamobile.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel()
{
    fun logout() = authRepository.clear()
}