package de.mistatee.erglog.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mistatee.erglog.data.auth.AuthRepository
import de.mistatee.erglog.data.auth.buildAuthorizationUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginScreenViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val mutableUiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = mutableUiState

    val authorizationUrl: String = buildAuthorizationUrl()

    /**
     * Called with the `code`/`error` query params parsed from the OAuth redirect Uri. Neither
     * being present (or `error` being present) means the Logbook Account holder cancelled or
     * denied consent in the Custom Tab.
     */
    fun onAuthorizationResult(code: String?, error: String?) {
        if (error != null || code == null) {
            mutableUiState.value = LoginUiState.Error("Login was cancelled or denied.")
            return
        }
        mutableUiState.value = LoginUiState.Loading
        viewModelScope.launch {
            authRepository
                .completeLogin(code)
                .onSuccess { mutableUiState.value = LoginUiState.LoggedIn }
                .onFailure {
                    mutableUiState.value = LoginUiState.Error(it.message ?: "Login failed.")
                }
        }
    }

    fun onRetry() {
        mutableUiState.value = LoginUiState.Idle
    }
}

sealed interface LoginUiState {
    data object Idle : LoginUiState

    data object Loading : LoginUiState

    data object LoggedIn : LoginUiState

    data class Error(val message: String) : LoginUiState
}
