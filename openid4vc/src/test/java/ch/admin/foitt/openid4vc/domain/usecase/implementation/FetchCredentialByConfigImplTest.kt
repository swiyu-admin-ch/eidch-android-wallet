package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.VerifiableCredentialParams
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredentialResult
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.VcSdJwtCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionType
import ch.admin.foitt.openid4vc.domain.usecase.FetchCredentialByConfig
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchVcSdJwtCredential
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FetchCredentialByConfigImplTest {

    @MockK
    private lateinit var mockFetchVcSdJwtCredential: FetchVcSdJwtCredential

    @MockK
    private lateinit var mockAnyCredentialResult: AnyCredentialResult

    @MockK
    private lateinit var mockVcSdJwtCredentialConfig: VcSdJwtCredentialConfiguration

    @MockK
    private lateinit var mockVerifiableCredentialParams: VerifiableCredentialParams

    @MockK
    private lateinit var mockPayloadEncryptionType: PayloadEncryptionType

    private lateinit var useCase: FetchCredentialByConfig

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = FetchCredentialByConfigImpl(
            fetchVcSdJwtCredential = mockFetchVcSdJwtCredential,
        )

        every { mockVerifiableCredentialParams.credentialConfiguration } returns mockVcSdJwtCredentialConfig
        every { mockVcSdJwtCredentialConfig.format } returns CredentialFormat.VC_SD_JWT
        coEvery {
            mockFetchVcSdJwtCredential(
                mockVerifiableCredentialParams,
                null,
                mockPayloadEncryptionType
            )
        } returns Ok(mockAnyCredentialResult)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Fetching credential by config with vc+sd_jwt config returns a valid credential`() = runTest {
        val result = useCase(
            mockVerifiableCredentialParams,
            null,
            mockPayloadEncryptionType
        )

        val credential = result.assertOk()
        assertEquals(mockAnyCredentialResult, credential)
    }

    @Test
    fun `Fetching vc+sd_jwt credential by config maps error from fetching jwt vc json credential`() = runTest {
        val exception = IllegalStateException()
        coEvery {
            mockFetchVcSdJwtCredential(any(), any(), any())
        } returns Err(CredentialOfferError.Unexpected(exception))

        val result = useCase(mockVerifiableCredentialParams, null, mockPayloadEncryptionType)

        val error = result.assertErrorType(CredentialOfferError.Unexpected::class)
        assertEquals(exception, error.cause)
    }
}
