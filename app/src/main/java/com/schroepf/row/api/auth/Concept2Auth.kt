package com.schroepf.row.api.auth

import android.net.Uri
import com.schroepf.row.BuildConfig
import com.schroepf.row.api.ApiConstants
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues

fun concept2AuthConfig(): Concept2AuthConfig = Concept2AuthConfig(
    clientId = BuildConfig.CONCEPT2_CLIENT_ID,
    clientSecret = BuildConfig.CONCEPT2_CLIENT_SECRET,
    redirectUri = Uri.parse(BuildConfig.CONCEPT2_REDIRECT_URI)
)

fun buildAuthorizationRequest(config: Concept2AuthConfig): AuthorizationRequest =
    AuthorizationRequest.Builder(
        AuthorizationServiceConfiguration(
            Uri.parse(ApiConstants.CONCEPT2_AUTHORIZATION_ENDPOINT),
            Uri.parse(ApiConstants.CONCEPT2_TOKEN_ENDPOINT)
        ),
        config.clientId,
        ResponseTypeValues.CODE,
        config.redirectUri
    ).setScope(ApiConstants.CONCEPT2_SCOPE).build()
