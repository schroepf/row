package de.mistatee.erglog.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mistatee.erglog.data.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LoginScreenViewModel(private val authRepository: AuthRepository) : ViewModel() {
    val uiState: StateFlow<LoginUiState>
        field = MutableStateFlow<LoginUiState>(LoginUiState.Idle)

    private val effectChannel = Channel<LoginEffect>(Channel.BUFFERED)
    val effects: Flow<LoginEffect> = effectChannel.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            LoginAction.LoginClicked ->
                viewModelScope.launch {
                    effectChannel.send(LoginEffect.OpenAuthorizationTab(authRepository.authorizationUrl))
                }

            is LoginAction.AuthorizationResultReceived -> onAuthorizationResult(action.code, action.error)
            LoginAction.RetryClicked -> uiState.value = LoginUiState.Idle
        }
    }

    /**
     * Called with the `code`/`error` query params parsed from the OAuth redirect Uri. Neither
     * being present (or `error` being present) means the Logbook Account holder cancelled or
     * denied consent in the Custom Tab.
     */
    private fun onAuthorizationResult(code: String?, error: String?) {
        if (error != null || code == null) {
            uiState.value = LoginUiState.Error("Login was cancelled or denied.")
            return
        }
        uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            authRepository
                .completeLogin(code)
                .onSuccess { uiState.value = LoginUiState.LoggedIn }
                .onFailure {
                    uiState.value = LoginUiState.Error(it.message ?: "Login failed.")
                }
        }
    }
}
