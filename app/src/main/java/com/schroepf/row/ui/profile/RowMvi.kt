package com.schroepf.row.ui.profile

import com.schroepf.row.api.log.UserProfile

sealed interface ProfileIntent {
    data class AuthorizationCodeReceived(val code: String, val codeVerifier: String?) : ProfileIntent
    data class LoginFailed(val error: String) : ProfileIntent
    data object Logout : ProfileIntent
}

sealed interface ProfileResult {
    data object Loading : ProfileResult
    data class Success(val profile: UserProfile) : ProfileResult
    data class Failure(val error: String) : ProfileResult
    data object LoggedOut : ProfileResult
}

data class ProfileState(
    val isLoading: Boolean = false,
    val profile: UserProfile? = null,
    val error: String? = null
)

object ProfileReducer {
    fun reduce(current: ProfileState, result: ProfileResult): ProfileState = when (result) {
        ProfileResult.Loading -> current.copy(isLoading = true, error = null)
        is ProfileResult.Success -> current.copy(isLoading = false, profile = result.profile, error = null)
        is ProfileResult.Failure -> current.copy(isLoading = false, profile = null, error = result.error)
        ProfileResult.LoggedOut -> ProfileState()
    }
}
