package com.schroepf.row

import android.net.Uri
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues

private const val CONCEPT2_AUTHORIZATION_ENDPOINT = "https://log.concept2.com/oauth/authorize"
const val CONCEPT2_TOKEN_ENDPOINT = "https://log.concept2.com/oauth/access_token"
const val CONCEPT2_PROFILE_ENDPOINT = "https://log.concept2.com/api/users/me"
private const val CONCEPT2_SCOPE = "user:read,results:read"

data class Concept2AuthConfig(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: Uri
)

fun concept2AuthConfig(): Concept2AuthConfig = Concept2AuthConfig(
    clientId = BuildConfig.CONCEPT2_CLIENT_ID,
    clientSecret = BuildConfig.CONCEPT2_CLIENT_SECRET,
    redirectUri = Uri.parse(BuildConfig.CONCEPT2_REDIRECT_URI)
)

fun buildAuthorizationRequest(config: Concept2AuthConfig): AuthorizationRequest =
    AuthorizationRequest.Builder(
        AuthorizationServiceConfiguration(
            Uri.parse(CONCEPT2_AUTHORIZATION_ENDPOINT),
            Uri.parse(CONCEPT2_TOKEN_ENDPOINT)
        ),
        config.clientId,
        ResponseTypeValues.CODE,
        config.redirectUri
    ).setScope(CONCEPT2_SCOPE).build()

fun concept2Scope(): String = CONCEPT2_SCOPE
