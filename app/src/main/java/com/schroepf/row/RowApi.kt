package com.schroepf.row

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface RowApi {
    suspend fun fetchWelcomeMessage(): String
}

class KtorRowApi(
    private val client: HttpClient,
    private val baseUrl: String = "https://jsonplaceholder.typicode.com"
) : RowApi {
    override suspend fun fetchWelcomeMessage(): String {
        val response = client.get("$baseUrl/posts/1").body<PostResponse>()
        return response.title
    }
}

@Serializable
data class PostResponse(
    @SerialName("title")
    val title: String
)
