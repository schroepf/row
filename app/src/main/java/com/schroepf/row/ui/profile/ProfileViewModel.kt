package com.schroepf.row.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.schroepf.row.api.auth.Concept2Auth
import com.schroepf.row.api.log.KtorRowApi
import com.schroepf.row.api.log.RowApi
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class ProfileViewModel(
    private val rowApi: RowApi
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    fun send(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.AuthorizationCodeReceived -> loadProfile(intent.code, intent.codeVerifier)
            is ProfileIntent.LoginFailed -> {
                _state.update { ProfileReducer.reduce(it, ProfileResult.Failure(intent.error)) }
            }

            ProfileIntent.Logout -> {
                _state.update { ProfileReducer.reduce(it, ProfileResult.LoggedOut) }
            }
        }
    }

    private fun loadProfile(authorizationCode: String, codeVerifier: String?) {
        _state.update { ProfileReducer.reduce(it, ProfileResult.Loading) }
        viewModelScope.launch {
            runCatching {
                rowApi.fetchUserProfile(authorizationCode, codeVerifier)
            }.onSuccess { profile ->
                _state.update { ProfileReducer.reduce(it, ProfileResult.Success(profile)) }
            }.onFailure { throwable ->
                _state.update {
                    ProfileReducer.reduce(
                        it,
                        ProfileResult.Failure(throwable.message ?: "Unexpected error")
                    )
                }
            }
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val client = HttpClient(CIO) {
                        install(ContentNegotiation) {
                            json(Json { ignoreUnknownKeys = true })
                        }
                    }
                    @Suppress("UNCHECKED_CAST")
                    return ProfileViewModel(KtorRowApi(client, Concept2Auth.concept2AuthConfig)) as T
                }
            }
    }
}
