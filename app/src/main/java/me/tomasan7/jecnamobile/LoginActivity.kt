package me.tomasan7.jecnamobile

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import me.tomasan7.jecnamobile.ui.components.OutlinedPasswordField
import me.tomasan7.jecnamobile.ui.theme.JecnaMobileTheme
import me.tomasan7.jecnamobile.ui.theme.md_theme_dark_background

class LoginActivity : ComponentActivity()
{
    private val viewModel: LoginScreenViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        viewModel.login.observe(this) { event ->
            event.handleIfNotHandledYet {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("username", it.username)
                intent.putExtra("password", it.password)
                startActivity(intent)
            }
        }

        setContent {
            JecnaMobileTheme {
                LoginScreen(viewModel)
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: LoginScreenViewModel = viewModel())
{
    if (viewModel.uiState.isLoading)
        LoadingState()
    else
        LoginState(viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginState(viewModel: LoginScreenViewModel)
{
    val state = viewModel.uiState

    Box(
        modifier = Modifier.fillMaxSize().background(md_theme_dark_background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.width(IntrinsicSize.Min),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OutlinedTextField(
                singleLine = true,
                isError = state.usernameBlankError,
                leadingIcon = { Icon(Icons.Default.Person, null) },
                label = { Text(stringResource(R.string.username)) },
                value = state.username,
                onValueChange = { viewModel.onFieldValueChange(true, it) },
                modifier = Modifier.onFocusEvent { }
            )

            if (state.usernameBlankError)
                Text(text = stringResource(R.string.username_error_blank),
                     style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.error,
                     modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 16.dp, end = 16.dp))

            Spacer(Modifier.height(10.dp))

            OutlinedPasswordField(
                singleLine = true,
                isError = state.passwordBlankError,
                leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                label = { Text(stringResource(R.string.password)) },
                value = state.password,
                onValueChange = { viewModel.onFieldValueChange(false, it) }
            )

            if (state.passwordBlankError)
                Text(
                    text = stringResource(R.string.password_error_blank),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 16.dp, end = 16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().clickable { viewModel.onRememberAuthCheckedChange(!state.rememberAuth) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start) {
                Checkbox(state.rememberAuth, onCheckedChange = { viewModel.onRememberAuthCheckedChange(it) })
                Text(stringResource(R.string.remember_auth_checkbox),
                     color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(20.dp))

            val loginEnabled = state.username.isNotBlank()
                               && state.password.isNotBlank()

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = loginEnabled,
                content = { Text(stringResource(R.string.login)) },
                onClick = { viewModel.onLoginClick() }
            )
        }
    }
}

@Composable
private fun LoadingState()
{
    Box(
        modifier = Modifier.fillMaxSize().background(md_theme_dark_background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Preview
@Composable
fun LoginScreenPreview()
{
    Surface {
        LoginScreen()
    }
}