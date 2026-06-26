package com.schroepf.row

import com.schroepf.row.ui.theme.RowTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import com.schroepf.row.api.auth.Concept2Auth
import com.schroepf.row.ui.profile.RowApp
import com.schroepf.row.ui.profile.ProfileIntent
import com.schroepf.row.ui.profile.ProfileViewModel
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService

class MainActivity : ComponentActivity() {
    private val viewModel: ProfileViewModel by viewModels { ProfileViewModel.factory() }
    private lateinit var authorizationService: AuthorizationService

    private val authorizationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val response = result.data?.let(AuthorizationResponse::fromIntent)
            val exception = result.data?.let(AuthorizationException::fromIntent)

            when {
                response?.authorizationCode != null -> {
                    viewModel.send(
                        ProfileIntent.AuthorizationCodeReceived(
                            code = response.authorizationCode!!,
                            codeVerifier = response.request.codeVerifier
                        )
                    )
                }

                exception != null -> {
                    viewModel.send(
                        ProfileIntent.LoginFailed(
                            exception.errorDescription ?: exception.error
                            ?: getString(R.string.login_failed_generic)
                        )
                    )
                }

                else -> {
                    viewModel.send(ProfileIntent.LoginFailed(getString(R.string.login_failed_generic)))
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authorizationService = AuthorizationService(this)
        setContent {
            RowTheme {
                Surface {
                    RowApp(
                        viewModel = viewModel,
                        onLogin = ::launchConcept2Login
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        authorizationService.dispose()
        super.onDestroy()
    }

    private fun launchConcept2Login() {
        authorizationLauncher.launch(
            authorizationService.getAuthorizationRequestIntent(
                Concept2Auth.concept2AuthorizationRequest
            )
        )
    }
}
