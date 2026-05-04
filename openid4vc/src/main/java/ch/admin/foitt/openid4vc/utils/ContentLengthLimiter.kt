package ch.admin.foitt.openid4vc.utils

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.statement.HttpReceivePipeline
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.util.AttributeKey
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.remaining
import io.ktor.utils.io.exhausted
import io.ktor.utils.io.readRemaining
import kotlinx.io.IOException
import kotlinx.io.readByteArray

class ContentLengthLimiter(
    config: ContentLengthLimiterConfig = ContentLengthLimiterConfig()
) {
    private val maxBytes: Long = config.limitInBytes

    companion object Plugin : HttpClientPlugin<ContentLengthLimiterConfig, ContentLengthLimiter> {
        override val key = AttributeKey<ContentLengthLimiter>("ContentLengthLimiter")

        override fun prepare(block: ContentLengthLimiterConfig.() -> Unit): ContentLengthLimiter {
            val config = ContentLengthLimiterConfig().apply(block)
            return ContentLengthLimiter(config)
        }

        override fun install(plugin: ContentLengthLimiter, scope: HttpClient) {
            scope.receivePipeline.intercept(HttpReceivePipeline.Before) { response ->
                val receivedContentLength = response.headers[HttpHeaders.ContentLength]?.toLong()

                if (receivedContentLength != null) {
                    // Check if Content-Length header is within the limit
                    // Ktor will throw later if Content-Length does not match actual content size
                    if (receivedContentLength > plugin.maxBytes) {
                        throw IOException("Content-Length exceeds limit: $receivedContentLength > ${plugin.maxBytes}")
                    }
                } else {
                    // Without Content-Length header we have to count the actual response size
                    val channel: ByteReadChannel = response.bodyAsChannel()
                    val chunkSize = 1024 * 1024
                    var counted: Long = 0

                    while (!channel.exhausted()) {
                        val chunk = channel.readRemaining(chunkSize.toLong())
                        counted += chunk.remaining

                        if (counted > plugin.maxBytes) {
                            throw IOException("Streamed content size exceeds limit: $counted > ${plugin.maxBytes}")
                        }

                        chunk.readByteArray()
                    }
                }

                proceedWith(
                    response
                )
            }
        }
    }
}

class ContentLengthLimiterConfig {
    var limitInBytes: Long = 150 * 1024 * 1024
}
