package de.mistatee.erglog.data.remote.profile

import de.mistatee.erglog.data.model.Profile
import de.mistatee.erglog.data.remote.profile.model.RemoteProfileException
import de.mistatee.erglog.data.remote.profile.model.RemoteProfileResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode

private const val PROFILE_URL = "https://log.concept2.com/api/users/me"

class Concept2RemoteProfileDataSource(
    private val httpClient: HttpClient,
) : RemoteProfileDataSource {
    override suspend fun fetchProfile(accessToken: String): Profile {
        val response =
            httpClient.get(PROFILE_URL) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
        if (response.status != HttpStatusCode.OK) {
            // There's no clean documented error shape for GET /api/users/{user}; fall back to a
            // generic error keyed on the HTTP status.
            throw RemoteProfileException(error = "http_${response.status.value}", errorDescription = null)
        }
        return response.body<RemoteProfileResponse>().data.let { Profile(username = it.username) }
    }
}
