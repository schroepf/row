package com.schroepf.row.api.auth

import com.schroepf.row.BuildConfig

data class Concept2AuthConfig(
    val clientId: String = BuildConfig.CONCEPT2_CLIENT_ID,
    val clientSecret: String = BuildConfig.CONCEPT2_CLIENT_SECRET,
    val redirectUri: String = BuildConfig.CONCEPT2_REDIRECT_URI,
)
