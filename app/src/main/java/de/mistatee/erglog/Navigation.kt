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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import de.mistatee.erglog.data.auth.DefaultAuthRepository
import de.mistatee.erglog.data.auth.EncryptedSessionStore
import de.mistatee.erglog.data.auth.KtorAuthApi
import de.mistatee.erglog.data.profile.DefaultProfileRepository
import de.mistatee.erglog.data.profile.KtorProfileApi
import de.mistatee.erglog.ui.login.LoginScreen
import de.mistatee.erglog.ui.main.MainScreen
import de.mistatee.erglog.ui.main.MainScreenViewModel

@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val authRepository =
        remember { DefaultAuthRepository(KtorAuthApi(), EncryptedSessionStore(context.applicationContext)) }
    val profileRepository = remember { DefaultProfileRepository(authRepository, KtorProfileApi()) }
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
                        viewModel = viewModel { MainScreenViewModel(profileRepository) },
                        modifier = Modifier
                            .safeDrawingPadding()
                            .padding(16.dp)
                    )
                }
            },
    )
}
