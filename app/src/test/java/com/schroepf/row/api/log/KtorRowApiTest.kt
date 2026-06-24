package com.schroepf.row.api.log

import com.schroepf.row.api.ApiException
import com.schroepf.row.api.auth.Concept2AuthConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class KtorRowApiTest {

    private val authConfig = Concept2AuthConfig(
        clientId = "test-client-id",
        clientSecret = "test-client-secret",
        redirectUri = "row://oauth/callback"
    )

    private fun createApi(engine: MockEngine): KtorRowApi =
        KtorRowApi(
            client = HttpClient(engine) {
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            },
            authConfig = authConfig
        )

    private fun tokenOkResponse() = Triple(
        """{"access_token":"test-access-token"}""",
        HttpStatusCode.OK,
        headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    )

    private fun profileOkResponse(username: String = "davidhart") = Triple(
        """{"data":{"username":"$username","first_name":"David","last_name":"Hart","email":"davidh@concept2.com","country":"GBR"}}""",
        HttpStatusCode.OK,
        headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    )

    // ── Happy path ───────────────────────────────────────────────────────────

    @Test
    fun `fetchUserProfile returns correct UserProfile on success`() = runTest {
        val engine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/oauth/access_token" -> tokenOkResponse().let { (c, s, h) -> respond(c, s, h) }
                "/api/users/me" -> profileOkResponse().let { (c, s, h) -> respond(c, s, h) }
                else -> error("Unexpected URL: ${request.url}")
            }
        }

        val result = createApi(engine).fetchUserProfile("auth-code", codeVerifier = null)

        assertEquals("davidhart", result.username)
        assertEquals("David Hart", result.fullName)
        assertEquals("davidh@concept2.com", result.email)
        assertEquals("GBR", result.country)
    }

    @Test
    fun `fetchUserProfile falls back to username as fullName when name fields are absent`() = runTest {
        val engine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/oauth/access_token" -> tokenOkResponse().let { (c, s, h) -> respond(c, s, h) }
                "/api/users/me" -> respond(
                    content = """{"data":{"username":"davidhart"}}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
                else -> error("Unexpected URL: ${request.url}")
            }
        }

        val result = createApi(engine).fetchUserProfile("auth-code", codeVerifier = null)

        assertEquals("davidhart", result.fullName)
    }

    // ── Token request format ─────────────────────────────────────────────────

    @Test
    fun `fetchUserProfile sends token request as POST with form-encoded body`() = runTest {
        var capturedMethod: HttpMethod? = null
        var capturedContentType = ""
        val engine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/oauth/access_token" -> {
                    capturedMethod = request.method
                    capturedContentType = request.body.contentType.toString()
                    tokenOkResponse().let { (c, s, h) -> respond(c, s, h) }
                }
                "/api/users/me" -> profileOkResponse().let { (c, s, h) -> respond(c, s, h) }
                else -> error("Unexpected URL: ${request.url}")
            }
        }

        createApi(engine).fetchUserProfile("auth-code", codeVerifier = null)

        assertEquals(HttpMethod.Post, capturedMethod)
        assertTrue(capturedContentType.contains("application/x-www-form-urlencoded"))
    }

    @Test
    fun `fetchUserProfile includes code_verifier in token request when non-null`() = runTest {
        var capturedBody = ""
        val engine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/oauth/access_token" -> {
                    capturedBody = (request.body as OutgoingContent.ByteArrayContent).bytes().decodeToString()
                    tokenOkResponse().let { (c, s, h) -> respond(c, s, h) }
                }
                "/api/users/me" -> profileOkResponse().let { (c, s, h) -> respond(c, s, h) }
                else -> error("Unexpected URL: ${request.url}")
            }
        }

        createApi(engine).fetchUserProfile("auth-code", codeVerifier = "test-verifier")

        assertTrue(capturedBody.contains("code_verifier=test-verifier"))
    }

    @Test
    fun `fetchUserProfile omits code_verifier from token request when null`() = runTest {
        var capturedBody = ""
        val engine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/oauth/access_token" -> {
                    capturedBody = (request.body as OutgoingContent.ByteArrayContent).bytes().decodeToString()
                    tokenOkResponse().let { (c, s, h) -> respond(c, s, h) }
                }
                "/api/users/me" -> profileOkResponse().let { (c, s, h) -> respond(c, s, h) }
                else -> error("Unexpected URL: ${request.url}")
            }
        }

        createApi(engine).fetchUserProfile("auth-code", codeVerifier = null)

        assertFalse(capturedBody.contains("code_verifier"))
    }

    // ── Profile request format ───────────────────────────────────────────────

    @Test
    fun `fetchUserProfile forwards access token as Bearer header in profile request`() = runTest {
        var capturedAuthHeader = ""
        var capturedAcceptHeader = ""
        val engine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/oauth/access_token" -> respond(
                    content = """{"access_token":"the-access-token"}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
                "/api/users/me" -> {
                    capturedAuthHeader = request.headers[HttpHeaders.Authorization] ?: ""
                    capturedAcceptHeader = request.headers[HttpHeaders.Accept] ?: ""
                    profileOkResponse().let { (c, s, h) -> respond(c, s, h) }
                }
                else -> error("Unexpected URL: ${request.url}")
            }
        }

        createApi(engine).fetchUserProfile("auth-code", codeVerifier = null)

        assertEquals("Bearer the-access-token", capturedAuthHeader)
        assertTrue(capturedAcceptHeader.contains("application/vnd.c2logbook.v1+json"))
    }

    // ── Token exchange failures ──────────────────────────────────────────────

    @Test
    fun `fetchUserProfile throws ApiException with status on token exchange HTTP error`() = runTest {
        val engine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/oauth/access_token" -> respond(content = "", status = HttpStatusCode.Unauthorized)
                else -> error("Unexpected URL: ${request.url}")
            }
        }

        try {
            createApi(engine).fetchUserProfile("bad-code", codeVerifier = null)
            fail("Expected ApiException")
        } catch (e: ApiException) {
            assertEquals(HttpStatusCode.Unauthorized, e.status)
        }
    }

    @Test
    fun `fetchUserProfile uses error_description from token error response body as exception message`() = runTest {
        val engine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/oauth/access_token" -> respond(
                    content = """{"error_description":"invalid_grant"}""",
                    status = HttpStatusCode.Unauthorized,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
                else -> error("Unexpected URL: ${request.url}")
            }
        }

        try {
            createApi(engine).fetchUserProfile("bad-code", codeVerifier = null)
            fail("Expected ApiException")
        } catch (e: ApiException) {
            assertEquals("invalid_grant", e.message)
        }
    }

    @Test
    fun `fetchUserProfile uses fallback message on non-JSON token error response`() = runTest {
        val engine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/oauth/access_token" -> respond(
                    content = "Internal Server Error",
                    status = HttpStatusCode.InternalServerError,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Plain.toString())
                )
                else -> error("Unexpected URL: ${request.url}")
            }
        }

        try {
            createApi(engine).fetchUserProfile("auth-code", codeVerifier = null)
            fail("Expected ApiException")
        } catch (e: ApiException) {
            assertEquals(HttpStatusCode.InternalServerError, e.status)
            assertTrue(e.message!!.contains("Concept2 token exchange failed with HTTP"))
        }
    }

    @Test
    fun `fetchUserProfile does not call profile endpoint when token exchange fails`() = runTest {
        var profileCalled = false
        val engine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/oauth/access_token" -> respond(content = "", status = HttpStatusCode.Unauthorized)
                "/api/users/me" -> {
                    profileCalled = true
                    profileOkResponse().let { (c, s, h) -> respond(c, s, h) }
                }
                else -> error("Unexpected URL: ${request.url}")
            }
        }

        try {
            createApi(engine).fetchUserProfile("bad-code", codeVerifier = null)
        } catch (_: ApiException) { /* expected */ }

        assertFalse(profileCalled)
    }

    // ── Profile request failures ─────────────────────────────────────────────

    @Test
    fun `fetchUserProfile throws ApiException with status on profile request HTTP error`() = runTest {
        val engine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/oauth/access_token" -> tokenOkResponse().let { (c, s, h) -> respond(c, s, h) }
                "/api/users/me" -> respond(content = "", status = HttpStatusCode.Forbidden)
                else -> error("Unexpected URL: ${request.url}")
            }
        }

        try {
            createApi(engine).fetchUserProfile("auth-code", codeVerifier = null)
            fail("Expected ApiException")
        } catch (e: ApiException) {
            assertEquals(HttpStatusCode.Forbidden, e.status)
        }
    }

    @Test
    fun `fetchUserProfile uses error_description from profile error response body as exception message`() = runTest {
        val engine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/oauth/access_token" -> tokenOkResponse().let { (c, s, h) -> respond(c, s, h) }
                "/api/users/me" -> respond(
                    content = """{"error_description":"forbidden"}""",
                    status = HttpStatusCode.Forbidden,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
                else -> error("Unexpected URL: ${request.url}")
            }
        }

        try {
            createApi(engine).fetchUserProfile("auth-code", codeVerifier = null)
            fail("Expected ApiException")
        } catch (e: ApiException) {
            assertEquals("forbidden", e.message)
        }
    }
}
