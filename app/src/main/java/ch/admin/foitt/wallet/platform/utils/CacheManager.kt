package ch.admin.foitt.wallet.platform.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class CacheManager<T : Any>(
    private val scope: CoroutineScope,
    private val timeToLive: Duration = 5.minutes,
) {
    private val mutex = Mutex()
    private var cached: T? = null
    private var expiresAt: Instant = Instant.MIN
    private var ongoingFetch: Deferred<T>? = null

    suspend fun get(fetch: suspend () -> T): T {
        val deferred = mutex.withLock {
            cached?.let { if (Instant.now() < expiresAt) return it }
            ongoingFetch ?: scope.async {
                try {
                    fetch().also { fresh ->
                        mutex.withLock {
                            cached = fresh
                            expiresAt = Instant.now().plus(timeToLive.toJavaDuration())
                        }
                    }
                } finally {
                    mutex.withLock { ongoingFetch = null }
                }
            }.also { ongoingFetch = it }
        }
        return deferred.await()
    }
}
