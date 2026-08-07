package de.mistatee.erglog.ui.login

import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.mistatee.erglog.data.auth.AuthRedirectHolder
import de.mistatee.erglog.data.auth.DefaultAuthRepository
import de.mistatee.erglog.data.auth.EncryptedSessionStore
import de.mistatee.erglog.data.auth.KtorAuthApi

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val viewModel: LoginScreenViewModel =
        viewModel {
            LoginScreenViewModel(
                DefaultAuthRepository(KtorAuthApi(), EncryptedSessionStore(context.applicationContext)),
            )
        }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        AuthRedirectHolder.redirectUris.collect { uri ->
            viewModel.onAuthorizationResult(
                code = uri.getQueryParameter("code"),
                error = uri.getQueryParameter("error"),
            )
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
                Button(onClick = {
                    CustomTabsIntent
                        .Builder()
                        .build()
                        .launchUrl(context, viewModel.authorizationUrl.toUri())
                }) {
                    Text("Log in with Concept2")
                }
            }

            LoginUiState.Loading -> {
                CircularProgressIndicator()
            }

            is LoginUiState.Error -> {
                Text(currentState.message)
                Button(onClick = viewModel::onRetry) {
                    Text("Retry")
                }
            }

            LoginUiState.LoggedIn -> {
                // Handled via the LaunchedEffect above; nothing to render.
            }
        }
    }
}
