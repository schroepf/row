package de.mistatee.erglog.ui.login

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.mistatee.erglog.data.auth.AuthRedirectHolder
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val viewModel: LoginScreenViewModel = koinViewModel<LoginScreenViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        AuthRedirectHolder.redirectUris.collect { uri ->
            viewModel.onAction(
                LoginAction.AuthorizationResultReceived(
                    code = uri.getQueryParameter("code"),
                    error = uri.getQueryParameter("error"),
                ),
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is LoginEffect.OpenAuthorizationTab ->
                    CustomTabsIntent.Builder().build().launchUrl(context, effect.url.toUri())
            }
        }
    }

    LaunchedEffect(state) {
        if (state is LoginUiState.LoggedIn) onLoginSuccess()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (val currentState = state) {
            LoginUiState.Idle -> {
                Button(onClick = { viewModel.onAction(LoginAction.LoginClicked) }) {
                    Text("Log in with Concept2")
                }
            }

            LoginUiState.Loading -> {
                CircularProgressIndicator()
            }

            is LoginUiState.Error -> {
                Text(currentState.message)
                Button(onClick = { viewModel.onAction(LoginAction.RetryClicked) }) {
                    Text("Retry")
                }
            }

            LoginUiState.LoggedIn -> {
                // Handled via the LaunchedEffect above; nothing to render.
            }
        }
    }
}
