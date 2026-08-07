package de.mistatee.erglog

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import de.mistatee.erglog.data.auth.AuthRedirectHolder
import de.mistatee.erglog.theme.ErgLogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        forwardAuthorizationRedirect(intent)
        setContent {
            ErgLogTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) { MainNavigation() }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        forwardAuthorizationRedirect(intent)
    }

    private fun forwardAuthorizationRedirect(intent: Intent) {
        val uri = intent.data ?: return
        if (uri.scheme == "de.mistatee.erglog" && uri.host == "authorization") {
            AuthRedirectHolder.onRedirect(uri)
        }
    }
}
