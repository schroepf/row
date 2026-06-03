package com.schroepf.row.api.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Concept2ErrorResponse(
    @SerialName("error_description")
    val errorDescription: String? = null
)
