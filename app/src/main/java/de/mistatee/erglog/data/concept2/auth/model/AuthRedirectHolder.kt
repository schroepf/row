package de.mistatee.erglog.data.concept2.auth.model

import android.net.Uri
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * App-scoped holder that carries the OAuth redirect [android.net.Uri] from [de.mistatee.erglog.MainActivity]
 * (where the Custom Tab redirect Intent lands) to the Login screen that is waiting for it.
 */
object AuthRedirectHolder {
    private val redirects = MutableSharedFlow<Uri>(extraBufferCapacity = 1)
    val redirectUris: SharedFlow<Uri> = redirects.asSharedFlow()

    fun onRedirect(uri: Uri) {
        redirects.tryEmit(uri)
    }
}
