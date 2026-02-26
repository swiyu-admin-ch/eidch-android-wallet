package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.foitt.openid4vc.domain.model.VerifiableCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionType
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.usecase.FetchVerifiableCredential
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchVcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.VerifyVcSdJwtSignature
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FetchVcSdJwtCredentialImplTest {
    @MockK
    private lateinit var mockFetchVerifiableCredential: FetchVerifiableCredential

    @MockK
    private lateinit var mockVerifyVcSdJwtSignature: VerifyVcSdJwtSignature

    @MockK
    private lateinit var mockPayloadEncryptionType: PayloadEncryptionType

    private lateinit var useCase: FetchVcSdJwtCredential

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = FetchVcSdJwtCredentialImpl(
            fetchVerifiableCredential = mockFetchVerifiableCredential,
            verifyVcSdJwtSignature = mockVerifyVcSdJwtSignature,
        )

        initDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Fetching a vc sd jwt credential with valid params returns a VcSdJwtCredential`() = runTest {
        val credential = useCase(
            verifiableCredentialParams = MockCredentialOffer.verifiableCredentialParamsWithoutBinding,
            bindingKeyPairs = null,
            payloadEncryptionType = mockPayloadEncryptionType,
        ).assertOk() as AnyVerifiedCredential

        assertEquals(null, credential.vcSdJwtCredential.keyBinding)
        assertEquals(VALID_JWT, credential.vcSdJwtCredential.payload)
    }

    @Test
    fun `Fetching a vc sd jwt credential where the jwt signature validation fails returns an error`() = runTest {
        coEvery {
            mockVerifyVcSdJwtSignature(any(), any())
        } returns Err(VcSdJwtError.InvalidJwt)

        useCase(
            verifiableCredentialParams = MockCredentialOffer.verifiableCredentialParamsWithoutBinding,
            bindingKeyPairs = null,
            payloadEncryptionType = mockPayloadEncryptionType,
        ).assertErrorType(CredentialOfferError.IntegrityCheckFailed::class)
    }

    @Test
    fun `Fetching a deferred vc sd jwt credential returns a deferred credential`() = runTest {
        coEvery {
            mockFetchVerifiableCredential(
                MockCredentialOffer.verifiableCredentialParamsWithoutBinding,
                bindingKeyPairs = null,
                mockPayloadEncryptionType
            )
        } returns Ok(MockCredentialOffer.validDeferredCredential)

        val deferredCredential = useCase(
            verifiableCredentialParams = MockCredentialOffer.verifiableCredentialParamsWithoutBinding,
            bindingKeyPairs = null,
            payloadEncryptionType = mockPayloadEncryptionType
        ).assertOk()

        assertEquals(MockCredentialOffer.validDeferredCredential, deferredCredential)
    }

    private fun initDefaultMocks() {
        coEvery {
            mockFetchVerifiableCredential(
                MockCredentialOffer.verifiableCredentialParamsWithoutBinding,
                bindingKeyPairs = null,
                mockPayloadEncryptionType
            )
        } returns Ok(createVerifiableCredential(VALID_JWT))

        coEvery {
            mockVerifyVcSdJwtSignature(any(), any(),)
        } returns Ok(validSdJwt)
    }

    private fun createVerifiableCredential(jwt: String) = VerifiableCredential(
        credential = jwt,
        keyBinding = null
    )

    private val validSdJwt = createVcSdJwtCredential(VALID_JWT)

    private fun createVcSdJwtCredential(jwt: String) = VcSdJwtCredential(
        1L,
        keyBinding = null,
        payload = jwt,
    )

    private companion object {
        const val VALID_JWT =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InR5cGUiLCJraWQiOiJrZXlJZCJ9.eyJpc3MiOiJpc3N1ZXIiLCJzdWIiOiJzdWJqZWN0IiwiZXhwIjoxOTI0OTg4Mzk5LCJpYXQiOjAsIm5iZiI6MSwidmN0IjoidmN0In0.jX5Mfxyh_gJ9VhagwlL80QFZjNgOPgdASjP3awIX-ty_LimDlNDZY3eCpjyecqcKFskkVx55gFs9h8_sENvNyQ"
    }
}
