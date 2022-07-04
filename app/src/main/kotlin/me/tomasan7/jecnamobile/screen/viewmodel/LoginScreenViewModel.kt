package me.tomasan7.jecnamobile.screen.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import me.tomasan7.jecnaapi.web.JecnaWebClient
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.screen.view.LoginScreenState
import me.tomasan7.jecnamobile.util.Event
import me.tomasan7.jecnamobile.util.asEvent
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val jecnaClient: JecnaWebClient
) : ViewModel()
{

    private val authPreferences
        get() = context.getSharedPreferences(AUTH_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)

    var uiState by mutableStateOf(LoginScreenState())
        private set

    val login = MutableLiveData<Event<JecnaWebClient>>()

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
            val successful = try
            {
                jecnaClient.login(username, password)
            }
            catch (e: Exception)
            {
                Toast.makeText(context, context.getString(R.string.login_error), Toast.LENGTH_LONG).show()
                false
            }

            if (successful)
            {
                login.value = jecnaClient.asEvent()

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