package com.schroepf.row.api.auth

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

suspend inline fun <reified T> HttpResponse.requireSuccess(operationName: String): T {
    if (!status.isSuccess()) {
        val error = runCatching { body<Concept2ErrorResponse>() }.getOrNull()
        throw IllegalStateException(error?.errorDescription ?: "$operationName failed with HTTP $status")
    }
    return body()
}
