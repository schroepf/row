package de.mistatee.erglog.ui.login

sealed interface LoginAction {
    data object LoginClicked : LoginAction

    data class AuthorizationResultReceived(val code: String?, val error: String?) : LoginAction

    data object RetryClicked : LoginAction
}
