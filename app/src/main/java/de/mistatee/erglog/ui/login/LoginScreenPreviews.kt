package de.mistatee.erglog.ui.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.mistatee.erglog.theme.ErgLogTheme

@Preview(showBackground = true, name = "Idle")
@Composable
fun LoginScreenIdlePreview() {
    ErgLogTheme {
        LoginScreen(
            state = LoginUiState.Idle,
            onAction = {},
        )
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
fun LoginScreenLoadingPreview() {
    ErgLogTheme {
        LoginScreen(
            state = LoginUiState.Loading,
            onAction = {},
        )
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
fun LoginScreenErrorPreview() {
    ErgLogTheme {
        LoginScreen(
            state = LoginUiState.Error("Login was cancelled or denied."),
            onAction = {},
        )
    }
}
