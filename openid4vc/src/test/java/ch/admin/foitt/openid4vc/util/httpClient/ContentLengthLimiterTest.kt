package ch.admin.foitt.openid4vc.util.httpClient

import ch.admin.foitt.openid4vc.utils.ContentLengthLimiter
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ContentLengthLimiterTest {
    private val contentLength = 100

    @Test
    fun `GET with content-length header and content length within limit succeeds`() = runTest {
        val client = createClient(setContentLengthHeader = true)
        val response = client.get("https://example.com")
        assertTrue(response.status == HttpStatusCode.OK) { "Return ok" }
        assertTrue(response.body<String>().contains("x"))
    }

    @Test
    fun `GET without content-length header, but content length within limit succeeds`() = runTest {
        val client = createClient(setContentLengthHeader = false)
        val response = client.get("https://example.com")
        assertTrue(response.status == HttpStatusCode.OK) { "Return ok" }
        assertTrue(response.body<String>().contains("x"))
    }

    @Test
    fun `GET with valid content-length header throws if content length exceeds limit`() = runTest {
        val client = createClient(setContentLengthHeader = true, contentLengthLimit = contentLength.toLong() - 1)
        val exception = assertThrows<IOException> {
            client.get("https://example.com")
        }
        assertTrue(exception.message?.startsWith("Content-Length exceeds limit", ignoreCase = true) ?: false)
    }

    @Test
    fun `GET without content-length header throws if content length exceeds limit`() = runTest {
        val client = createClient(setContentLengthHeader = false, contentLengthLimit = contentLength.toLong() - 1)
        val exception = assertThrows<IOException> {
            client.get("https://example.com")
        }
        assertTrue(exception.message?.startsWith("streamed content size exceeds limit", ignoreCase = true) ?: false)
    }

    private fun createClient(setContentLengthHeader: Boolean, contentLengthLimit: Long = contentLength.toLong()): HttpClient {
        val mockEngine = MockEngine.Companion {
            respond(
                content = "x".repeat(contentLength),
                status = HttpStatusCode.OK,
                headers = if (setContentLengthHeader) {
                    headersOf(
                        HttpHeaders.ContentLength,
                        contentLength.toString()
                    )
                } else {
                    headersOf()
                }
            )
        }
        return HttpClient(mockEngine) {
            install(ContentLengthLimiter) {
                limitInBytes = contentLengthLimit
            }
        }
    }
}
