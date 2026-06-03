package com.schroepf.row

sealed interface RowIntent {
    data object Refresh : RowIntent
}

sealed interface RowResult {
    data object Loading : RowResult
    data class Success(val message: String) : RowResult
    data class Failure(val error: String) : RowResult
}

data class RowState(
    val isLoading: Boolean = false,
    val message: String = "",
    val error: String? = null
)

object RowReducer {
    fun reduce(current: RowState, result: RowResult): RowState = when (result) {
        RowResult.Loading -> current.copy(isLoading = true, error = null)
        is RowResult.Success -> current.copy(isLoading = false, message = result.message, error = null)
        is RowResult.Failure -> current.copy(isLoading = false, error = result.error)
    }
}
