package de.mistatee.erglog.ui.main

sealed interface MainScreenUiState {
    object Loading : MainScreenUiState

    data class Error(val throwable: Throwable) : MainScreenUiState

    data class Success(val username: String) : MainScreenUiState
}
