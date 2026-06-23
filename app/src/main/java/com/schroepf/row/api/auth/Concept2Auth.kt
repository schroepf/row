package com.schroepf.row.api.auth

import androidx.core.net.toUri
import com.schroepf.row.api.ApiConstants
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues


object Concept2Auth {
    val concept2AuthConfig: Concept2AuthConfig = Concept2AuthConfig()

    val concept2AuthorizationRequest: AuthorizationRequest = AuthorizationRequest.Builder(
        AuthorizationServiceConfiguration(
            ApiConstants.CONCEPT2_AUTHORIZATION_ENDPOINT.toUri(),
            ApiConstants.CONCEPT2_TOKEN_ENDPOINT.toUri()
        ),
        concept2AuthConfig.clientId,
        ResponseTypeValues.CODE,
        concept2AuthConfig.redirectUri.toUri()
    )
        .setScope(ApiConstants.CONCEPT2_SCOPE)
        .build()
}
