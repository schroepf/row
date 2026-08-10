package de.mistatee.erglog.ui.login

sealed interface LoginUiState {
    data object Idle : LoginUiState

    data object Loading : LoginUiState

    data object LoggedIn : LoginUiState

    data class Error(val message: String) : LoginUiState
}
