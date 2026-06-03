package com.schroepf.row.api.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Concept2TokenResponse(
    @SerialName("access_token")
    val accessToken: String
)
