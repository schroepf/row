package com.schroepf.row.api.log

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Concept2UserEnvelope(
    @SerialName("data")
    val data: Concept2UserResponse
)
