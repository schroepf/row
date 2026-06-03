package com.schroepf.row.api.auth

import android.net.Uri

data class Concept2AuthConfig(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: Uri
)
