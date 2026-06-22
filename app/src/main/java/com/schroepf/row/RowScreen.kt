package com.schroepf.row

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun RowApp(
    viewModel: RowViewModel,
    onLogin: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    RowScreen(
        state = state,
        onLogin = onLogin,
        onLogout = { viewModel.send(RowIntent.Logout) }
    )
}

@Composable
fun RowScreen(
    state: RowState,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            state.isLoading -> {
                Text(
                    text = stringResource(id = R.string.loading),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            }

            state.error != null -> {
                Text(
                    text = state.error,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            state.profile != null -> {
                Text(
                    text = stringResource(id = R.string.profile_title),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = stringResource(id = R.string.profile_username, state.profile.username),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = stringResource(id = R.string.profile_name, state.profile.fullName),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                state.profile.email?.let { email ->
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = stringResource(id = R.string.profile_email, email),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
                state.profile.country?.let { country ->
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = stringResource(id = R.string.profile_country, country),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                Text(
                    text = stringResource(id = R.string.login_prompt),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (state.profile != null) {
            Button(
                modifier = Modifier.padding(top = 16.dp),
                onClick = onLogout
            ) {
                Text(text = stringResource(id = R.string.logout))
            }
        } else {
            Button(
                modifier = Modifier.padding(top = 16.dp),
                onClick = onLogin,
                enabled = !state.isLoading
            ) {
                Text(text = stringResource(id = R.string.login))
            }
        }
    }
}
