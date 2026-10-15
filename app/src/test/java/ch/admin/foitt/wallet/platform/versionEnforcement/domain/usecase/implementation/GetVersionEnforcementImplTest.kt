package ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.VersionEnforcementError
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.VersionEnforcementRaw
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.repository.VersionEnforcementRepository
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.VerifyDetachedJws
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import ch.admin.foitt.wallet.util.assertOkNullable
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetVersionEnforcementImplTest {

    @MockK
    private lateinit var mockVersionEnforcementRepository: VersionEnforcementRepository

    @MockK
    private lateinit var mockVerifyDetachedJws: VerifyDetachedJws

    private val safeJson = SafeJsonTestInstance.safeJson

    private lateinit var useCase: GetVersionEnforcementImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = GetVersionEnforcementImpl(
            versionEnforcementRepository = mockVersionEnforcementRepository,
            verifyDetachedJws = mockVerifyDetachedJws,
            safeJson = safeJson,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `When repository returns null, should return Ok null`() = runTest {
        coEvery { mockVersionEnforcementRepository.fetchVersionEnforcement() } returns Ok(null)

        val result = useCase().assertOkNullable()

        assertNull(result)
    }

    @Test
    fun `When repository returns raw data but detachedJws is null, should return SignatureValidationError`() = runTest {
        coEvery { mockVersionEnforcementRepository.fetchVersionEnforcement() } returns Ok(
            VersionEnforcementRaw(body = VALID_BODY, detachedJws = null)
        )

        useCase().assertErrorType(VersionEnforcementError.SignatureValidationError::class)
    }

    @Test
    fun `When verification of detached JWS fails, should return the verification error`() = runTest {
        coEvery { mockVersionEnforcementRepository.fetchVersionEnforcement() } returns Ok(
            VersionEnforcementRaw(body = VALID_BODY, detachedJws = JWS)
        )
        coEvery { mockVerifyDetachedJws(JWS, VALID_BODY) } returns Err(VersionEnforcementError.SignatureValidationError)

        useCase().assertErrorType(VersionEnforcementError.SignatureValidationError::class)
    }

    @Test
    fun `When parsing JSON fails, should return Unexpected error`() = runTest {
        val invalidBody = "{ invalid json }"
        coEvery { mockVersionEnforcementRepository.fetchVersionEnforcement() } returns Ok(
            VersionEnforcementRaw(body = invalidBody, detachedJws = JWS)
        )
        coEvery { mockVerifyDetachedJws(JWS, invalidBody) } returns Ok(Unit)

        useCase().assertErrorType(VersionEnforcementError.Unexpected::class)
    }

    @Test
    fun `When repository returns valid raw data and verification succeeds, should parse and return Ok VersionEnforcement`() = runTest {
        coEvery { mockVersionEnforcementRepository.fetchVersionEnforcement() } returns Ok(
            VersionEnforcementRaw(body = VALID_BODY, detachedJws = JWS)
        )
        coEvery { mockVerifyDetachedJws(JWS, VALID_BODY) } returns Ok(Unit)

        val result = useCase().assertOk()

        assertEquals("ch.admin.foitt.wallet", result?.appId)
        assertEquals(30, result?.lifetime)
        assertEquals("https://store.url", result?.storeUrl)
    }

    private companion object {
        const val JWS = "some.detached.jws"
        const val VALID_BODY = """
        {
          "app_id": "ch.admin.foitt.wallet",
          "default_message": [
            {
              "title": "Title",
              "body": "Body text",
              "locale": "en"
            }
          ],
          "default_support_lifetime_days": 30,
          "device_blacklist": [],
          "minimum_os_version": "21",
          "platform": "android",
          "store_url": "https://store.url",
          "versions": [
            {
              "message": [
                {
                  "title": "Version Title",
                  "body": "Version Body text",
                  "locale": "en"
                }
              ],
              "release_date": "2023-11-17",
              "support_guaranteed_until": "2024-11-17",
              "update_type": "optional",
              "version": "1.0.0"
            }
          ]
        }
        """
    }
}
