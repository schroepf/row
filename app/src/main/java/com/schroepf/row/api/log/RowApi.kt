package com.schroepf.row.api.log

interface RowApi {
    suspend fun fetchUserProfile(authorizationCode: String, codeVerifier: String?): UserProfile
}
