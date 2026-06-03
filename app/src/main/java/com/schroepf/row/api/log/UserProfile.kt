package com.schroepf.row.api.log

data class UserProfile(
    val username: String,
    val fullName: String,
    val email: String?,
    val country: String?
)
