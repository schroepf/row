package de.mistatee.erglog.data.remote.results.model

import kotlinx.serialization.Serializable

@Serializable
data class RemoteMetaResponse(val pagination: RemotePaginationResponse)
