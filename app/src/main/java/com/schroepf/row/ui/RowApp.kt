package com.schroepf.row.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun RowApp(
    viewModel: ProfileViewModel,
    onLogin: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    ProfileScreen(
        state = state,
        onLogin = onLogin,
        onLogout = { viewModel.send(ProfileIntent.Logout) }
    )
}