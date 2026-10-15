package ch.admin.foitt.wallet.platform.versionEnforcement.data.repository

import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.utils.CacheManager
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.VersionEnforcementRaw
import ch.admin.foitt.wallet.util.assertOk
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VersionEnforcementRepositoryImplTest {

    @MockK
    private lateinit var mockEnvironmentSetupRepo: EnvironmentSetupRepository

    private lateinit var repository: VersionEnforcementRepositoryImpl

    private lateinit var cache: CacheManager<VersionEnforcementRaw>

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { mockEnvironmentSetupRepo.isVersionEnforcementEnabled } returns true
        every { mockEnvironmentSetupRepo.appVersionEnforcementUrl } returns "https://example.com"

        cache = CacheManager(CoroutineScope(Dispatchers.Unconfined))
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    private fun createRepository(engine: MockEngine) {
        val httpClient = HttpClient(engine)
        repository = VersionEnforcementRepositoryImpl(
            httpClient = httpClient,
            environmentSetupRepo = mockEnvironmentSetupRepo,
            cache = cache,
        )
    }

    @Test
    fun `Successfully fetching version enforcement with detached JWS returns raw data`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = BODY,
                status = HttpStatusCode.OK,
                headers = headersOf(
                    "detached-jws" to listOf(JWS),
                    HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString())
                )
            )
        }
        createRepository(mockEngine)

        val value = repository.fetchVersionEnforcement().assertOk()
        assertEquals(BODY, value?.body)
        assertEquals(JWS, value?.detachedJws)
    }

    @Test
    fun `Fetching version enforcement without signature still returns raw data`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = BODY,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        createRepository(mockEngine)

        val value = repository.fetchVersionEnforcement().assertOk()
        assertEquals(BODY, value?.body)
        assertEquals(null, value?.detachedJws)
    }

    private companion object {
        const val JWS = "some.detached.jws"
        const val BODY = """{"app_id": "ch.admin.foitt.wallet"}"""
    }
}
