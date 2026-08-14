package de.mistatee.erglog.data.remote.profile.model

import kotlinx.serialization.Serializable

/**
 * Response body from the Concept2 `/api/users/{user}` endpoint. Only the fields currently needed
 * by the app are modeled; the full response has additional fields not yet used here.
 */
@Serializable
data class RemoteProfileResponse(val data: RemoteProfileData)
