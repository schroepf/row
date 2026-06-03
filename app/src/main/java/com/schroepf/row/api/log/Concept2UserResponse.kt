package com.schroepf.row.api.log

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Concept2UserResponse(
    @SerialName("username")
    val username: String,
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("country")
    val country: String? = null
)

fun Concept2UserResponse.toUserProfile(): UserProfile {
    val fullName = listOfNotNull(firstName?.takeIf { it.isNotBlank() }, lastName?.takeIf { it.isNotBlank() })
        .joinToString(" ")
        .ifBlank { username }
    return UserProfile(
        username = username,
        fullName = fullName,
        email = email,
        country = country
    )
}
