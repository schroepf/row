package de.mistatee.erglog

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import de.mistatee.erglog.data.repository.AuthRepository
import de.mistatee.erglog.ui.login.LoginScreen
import de.mistatee.erglog.ui.main.MainScreen
import de.mistatee.erglog.ui.main.MainScreenViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun MainNavigation() {
    val authRepository = koinInject<AuthRepository>()
    var hasSession by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        hasSession = authRepository.currentSession() != null
    }

    val sessionKnown = hasSession
    if (sessionKnown == null) return

    val backStack = rememberNavBackStack(if (sessionKnown) Main else Login)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider =
            entryProvider {
                entry<Login> {
                    LoginScreen(
                        onLoginSuccess = {
                            backStack.clear()
                            backStack.add(Main)
                        },
                        modifier = Modifier
                            .safeDrawingPadding()
                            .padding(16.dp),
                    )
                }
                entry<Main> {
                    MainScreen(
                        viewModel = koinViewModel<MainScreenViewModel>(),
                        modifier = Modifier
                            .safeDrawingPadding()
                            .padding(16.dp),
                    )
                }
            },
    )
}
