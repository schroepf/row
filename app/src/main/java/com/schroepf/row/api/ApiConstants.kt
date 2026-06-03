package com.schroepf.row.api

object ApiConstants {
    const val CONCEPT2_AUTHORIZATION_ENDPOINT = "https://log.concept2.com/oauth/authorize"
    const val CONCEPT2_TOKEN_ENDPOINT = "https://log.concept2.com/oauth/access_token"
    const val CONCEPT2_PROFILE_ENDPOINT = "https://log.concept2.com/api/users/me"
    const val CONCEPT2_PROFILE_ACCEPT = "application/vnd.c2logbook.v1+json"
    const val CONCEPT2_SCOPE = "user:read,results:read"
}
