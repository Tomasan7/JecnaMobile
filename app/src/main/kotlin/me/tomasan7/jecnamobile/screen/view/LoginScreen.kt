package me.tomasan7.jecnamobile.screen.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import me.tomasan7.jecnamobile.R
import me.tomasan7.jecnamobile.destinations.LoginScreenDestination
import me.tomasan7.jecnamobile.destinations.MainScreenDestination
import me.tomasan7.jecnamobile.screen.viewmodel.LoginScreenViewModel
import me.tomasan7.jecnamobile.ui.component.OutlinedPasswordField

@RootNavGraph
@Destination
@Composable
fun LoginScreen(
    navigator: DestinationsNavigator,
    viewModel: LoginScreenViewModel = hiltViewModel()
)
{
    viewModel.login.observe(LocalLifecycleOwner.current) { event ->
        event.handleIfNotHandledYet {
            navigator.navigate(MainScreenDestination()) {
                popUpTo(LoginScreenDestination.route) {
                    inclusive = true
                }
            }
        }
    }

    if (viewModel.uiState.isLoading)
        LoadingState()
    else
        LoginState(viewModel)
}

@Composable
private fun LoadingState()
{
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginState(viewModel: LoginScreenViewModel = viewModel())
{
    val state = viewModel.uiState

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.login_title),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 100.dp))

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
                onValueChange = { viewModel.onFieldValueChange(true, it) }
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
                modifier = Modifier.fillMaxWidth()
                    .clickable { viewModel.onRememberAuthCheckedChange(!state.rememberAuth) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
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