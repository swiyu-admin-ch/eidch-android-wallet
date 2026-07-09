package ch.admin.foitt.wallet.platform.trustRegistry

import ch.admin.foitt.didResolver.domain.DidResolverHelper
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.GetTrustDomainFromDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustDomainFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.GetTrustDomainFromDidImpl
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class GetTrustDomainFromDidImplTest {
    @MockK
    private lateinit var mockDidResolverHelper: DidResolverHelper

    @MockK
    private lateinit var mockEnvironmentSetup: EnvironmentSetupRepository

    private lateinit var useCase: GetTrustDomainFromDid

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = GetTrustDomainFromDidImpl(
            didResolverHelper = mockDidResolverHelper,
            repo = mockEnvironmentSetup
        )

        every {
            mockDidResolverHelper.getHttpsUrl(did)
        } returns Ok(URL("https://$domain"))

        coEvery { mockEnvironmentSetup.trustRegistryMapping } returns trustRegistryMappings
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A Did with supported base domain returns the correct mapping`() {
        val result = useCase(
            actorDid = did,
        ).assertOk()

        assertEquals(trustRegistryMappings[domain], result)
        coVerify(exactly = 1) {
            mockEnvironmentSetup.trustRegistryMapping
        }
    }

    @Test
    fun `A did resolver helper error is mapped`() = runTest {
        val exception = IllegalStateException("did error")
        every { mockDidResolverHelper.getHttpsUrl(any()) } returns Err(exception)

        useCase(actorDid = did).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A Did with unsupported base domain returns an error`() {
        every {
            mockDidResolverHelper.getHttpsUrl(did)
        } returns Ok(URL("https://example.org"))
        useCase(actorDid = did).assertErrorType(GetTrustDomainFromDidError.NoTrustRegistryMapping::class)
    }

    private val domain = "dev.other.domain.swiyu.admin.ch"
    private val did = "did:tdw:randomid=:$domain:api:v1:did:randomuuid2"
    private val trustRegistryMappings = mapOf(
        domain to "dev.org",
    )
}
