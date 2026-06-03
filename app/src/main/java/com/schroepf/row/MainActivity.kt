package com.schroepf.row

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.schroepf.row.api.auth.buildAuthorizationRequest
import com.schroepf.row.api.auth.concept2AuthConfig
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService

class MainActivity : ComponentActivity() {
    private val viewModel: RowViewModel by viewModels { RowViewModel.factory() }
    private lateinit var authorizationService: AuthorizationService

    private val authorizationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val response = result.data?.let(AuthorizationResponse::fromIntent)
            val exception = result.data?.let(AuthorizationException::fromIntent)

            when {
                response?.authorizationCode != null -> {
                    viewModel.send(RowIntent.AuthorizationCodeReceived(response.authorizationCode!!))
                }

                exception != null -> {
                    viewModel.send(
                        RowIntent.LoginFailed(
                            exception.errorDescription ?: exception.error ?: getString(R.string.login_failed_generic)
                        )
                    )
                }

                else -> {
                    viewModel.send(RowIntent.LoginFailed(getString(R.string.login_failed_generic)))
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authorizationService = AuthorizationService(this)
        setContent {
            MaterialTheme {
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
                buildAuthorizationRequest(concept2AuthConfig())
            )
        )
    }
}
