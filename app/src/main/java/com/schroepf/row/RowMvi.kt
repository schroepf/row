package com.schroepf.row

import com.schroepf.row.api.log.UserProfile

sealed interface RowIntent {
    data class AuthorizationCodeReceived(val code: String) : RowIntent
    data class LoginFailed(val error: String) : RowIntent
}

sealed interface RowResult {
    data object Loading : RowResult
    data class Success(val profile: UserProfile) : RowResult
    data class Failure(val error: String) : RowResult
}

data class RowState(
    val isLoading: Boolean = false,
    val profile: UserProfile? = null,
    val error: String? = null
)

object RowReducer {
    fun reduce(current: RowState, result: RowResult): RowState = when (result) {
        RowResult.Loading -> current.copy(isLoading = true, error = null)
        is RowResult.Success -> current.copy(isLoading = false, profile = result.profile, error = null)
        is RowResult.Failure -> current.copy(isLoading = false, profile = null, error = result.error)
    }
}
