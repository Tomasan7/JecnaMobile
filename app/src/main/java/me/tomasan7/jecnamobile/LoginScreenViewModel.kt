package me.tomasan7.jecnamobile

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.tomasan7.jecnaapi.web.Auth
import me.tomasan7.jecnaapi.web.JecnaWebClient


class LoginScreenViewModel(application: Application) : AndroidViewModel(application)
{
    private val context
        get() = getApplication<Application>().applicationContext

    private val authPreferences
        get() = context.getSharedPreferences(AUTH_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)

    var uiState by mutableStateOf(LoginScreenState())
        private set

    val login = MutableLiveData<Event<Auth>>()

    init
    {
        if (authPreferences.contains(PREFERENCES_USERNAME_KEY))
            login(authPreferences.getString(PREFERENCES_USERNAME_KEY, null) as String, authPreferences.getString(PREFERENCES_PASSWORD_KEY, null) as String)
    }

    fun onFieldValueChange(username: Boolean, newValue: String)
    {
        /* Username value is changing. */
        if (username)
        {
            changeUiState(username = newValue)

            if (uiState.username.isBlank())
                changeUiState(usernameBlankError = true)
            else if (uiState.usernameBlankError && uiState.username.isNotBlank())
                changeUiState(usernameBlankError = false)
        }
        /* Password value is changing. */
        else
        {
            changeUiState(password = newValue)

            if (uiState.password.isBlank())
                changeUiState(passwordBlankError = true)
            else if (uiState.passwordBlankError && uiState.password.isNotBlank())
                changeUiState(passwordBlankError = false)
        }
    }

    fun onRememberAuthCheckedChange(newValue: Boolean) = changeUiState(rememberAuth = newValue)

    fun onLoginClick() = login()

    private fun login(username: String = uiState.username, password: String = uiState.password)
    {
        changeUiState(isLoading = true)

        viewModelScope.launch {
            val client = JecnaWebClient(username, password)
            val successful = client.login()

            if (successful)
            {
                login.value = Auth(username, password).asEvent()

                if (uiState.rememberAuth)
                    saveAuth()
            }
            else
                changeUiState(isLoading = false, username = "", password = "")
        }
    }

    private fun saveAuth()
    {
        with(authPreferences.edit()) {
            putString(PREFERENCES_USERNAME_KEY, uiState.username)
            putString(PREFERENCES_PASSWORD_KEY, uiState.password)
            apply()
        }
    }

    private fun changeUiState(isLoading: Boolean = uiState.isLoading,
                              username: String = uiState.username,
                              password: String = uiState.password,
                              usernameBlankError: Boolean = uiState.usernameBlankError,
                              passwordBlankError: Boolean = uiState.passwordBlankError,
                              rememberAuth: Boolean = uiState.rememberAuth)
    {
        uiState = uiState.copy(isLoading = isLoading,
                               username = username,
                               password = password,
                               usernameBlankError = usernameBlankError,
                               passwordBlankError = passwordBlankError,
                               rememberAuth = rememberAuth)
    }

    companion object
    {
        const val AUTH_PREFERENCES_FILE_NAME = "auth"
        const val PREFERENCES_USERNAME_KEY = "username"
        const val PREFERENCES_PASSWORD_KEY = "password"
    }
}