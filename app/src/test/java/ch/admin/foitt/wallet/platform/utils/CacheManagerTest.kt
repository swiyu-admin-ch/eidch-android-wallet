package ch.admin.foitt.wallet.platform.utils

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
class CacheManagerTest {

    @Test
    fun `Second fetch call returns cached value without fetching again`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val cache = CacheManager<String>(scope)
        var calls = 0
        val fetch: suspend () -> String = {
            calls++
            "value"
        }

        assertEquals("value", cache.get(fetch))
        assertEquals("value", cache.get(fetch))
        assertEquals(1, calls)
    }

    @Test
    fun `Concurrent fetch calls share a single in-flight fetch`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val cache = CacheManager<String>(scope)
        val gate = CompletableDeferred<Unit>()
        var calls = 0
        val fetch: suspend () -> String = {
            calls++
            gate.await()
            "value"
        }
        val a = async { cache.get(fetch) }
        val b = async { cache.get(fetch) }
        gate.complete(Unit)

        assertEquals("value", a.await())
        assertEquals("value", b.await())
        assertEquals(1, calls)
    }

    @Test
    fun `Refetches when the cached value has expired`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val cache = CacheManager<String>(scope, timeToLive = Duration.ZERO)
        var calls = 0
        val fetch: suspend () -> String = {
            calls++
            "value"
        }
        cache.get(fetch)
        cache.get(fetch)

        assertEquals(2, calls)
    }
}
