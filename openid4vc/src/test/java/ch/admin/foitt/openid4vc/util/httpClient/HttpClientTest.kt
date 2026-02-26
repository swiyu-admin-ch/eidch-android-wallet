package ch.admin.foitt.openid4vc.util.httpClient

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.network.sockets.SocketTimeoutException
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class HttpClientTest {
    private val requestTimeout = 100L
    private val socketTimeout = 100L

    private val mockHttpClientRequestTimeout = HttpClient(MockEngine) {
        install(HttpTimeout) {
            requestTimeoutMillis = requestTimeout
        }
        engine {
            addHandler { request ->
                when {
                    request.method == HttpMethod.Get && request.url.encodedPath == "/requestTimeout" -> {
                        delay(requestTimeout + 1000)
                        respond(
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.contentType),
                            content = "{}",
                        )
                    }

                    else -> error("Unhandled request")
                }
            }
        }
    }

    private val mockHttpClientSocketTimeout = HttpClient(OkHttp.create()) {
        install(HttpTimeout) {
            socketTimeoutMillis = socketTimeout
        }
    }

    val mockServer = embeddedServer(Netty, port = 8080) {
        routing {
            get("/socketTimeout") {
                delay(socketTimeout + 1)
            }
        }
    }

    @Test
    fun `Slow response triggers a request timeout`() = runTest {
        assertThrows<HttpRequestTimeoutException> {
            mockHttpClientRequestTimeout.get("https://example.com/requestTimeout")
        }
    }

    @Test
    fun `Long pause between data packets triggers a socket timeout`() = runTest {
        mockServer.start()

        assertThrows<SocketTimeoutException> {
            mockHttpClientSocketTimeout.get("http://localhost:8080/socketTimeout")
        }

        mockServer.stop()
    }
}
