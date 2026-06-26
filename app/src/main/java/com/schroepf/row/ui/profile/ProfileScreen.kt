package com.schroepf.row.ui.profile

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.schroepf.row.R
import com.schroepf.row.api.log.UserProfile
import com.schroepf.row.ui.theme.RowTheme

@Composable
fun ProfileScreen(
    state: ProfileState,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
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
                        text = stringResource(
                            id = R.string.profile_username,
                            state.profile.username
                        ),
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
}

@DefaultPreviews()
@Composable
fun ProfileScreenPreview(@PreviewParameter(ProfileStateProvider::class) state: ProfileState) {
    RowTheme {
        ProfileScreen(
            state = state,
            onLogin = {},
            onLogout = {}
        )
    }
}

class ProfileStateProvider : PreviewParameterProvider<ProfileState> {
    override val values = sequenceOf(
        ProfileState(
            isLoading = true,
        ),
        ProfileState(
            profile = UserProfile(
                username = "davidhart",
                fullName = "David Hart",
                email = "davidhart@gmail.com",
                country = "GBR",
            ),
        ),
        ProfileState(
            error = "invalid_grant",
        ),
    )
}

@Preview(
    name = "50% font",
    group = "font scale",
    fontScale = 0.5f,
)
@Preview(
    name = "150% font",
    group = "font scale",
    fontScale = 1.5f,
)
@Preview(
    name = "Light",
    group = "ui mode",
)
@Preview(
    name = "Dark",
    group = "ui mode",
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
)
annotation class DefaultPreviews
