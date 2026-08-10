package de.mistatee.erglog.ui.login

sealed interface LoginEffect {
    data class OpenAuthorizationTab(val url: String) : LoginEffect
}
