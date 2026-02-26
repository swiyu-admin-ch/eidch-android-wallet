package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.usecase.VerifyJwtSignature
import ch.admin.foitt.openid4vc.util.assertErrorType
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

class VerifyVcSdJwtSignatureImplTest {

    @MockK
    private lateinit var mockVerifyJwtSignature: VerifyJwtSignature

    private lateinit var useCase: VerifyVcSdJwtSignatureImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = VerifyVcSdJwtSignatureImpl(
            verifyJwtSignature = mockVerifyJwtSignature,
        )

        coEvery {
            mockVerifyJwtSignature(did = any(), kid = any(), jwt = any())
        } returns Ok(Unit)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Verifying a valid vc sd jwt succeeds`() = runTest {
        useCase(
            keyBinding = null,
            payload = VALID_JWT,
        )
    }

    @Test
    fun `Verifying a vc sd jwt credential that contains non disclosable claims returns an error`() = runTest {
        useCase(
            keyBinding = null,
            payload = JWT_WITH_NON_DISCLOSABLE_CLAIM,
        ).assertErrorType(VcSdJwtError.InvalidVcSdJwt::class)
    }

    @Test
    fun `Verifying a vc sd jwt credential that does not contain an issuer returns an error`() = runTest {
        useCase(
            keyBinding = null,
            payload = JWT_WITHOUT_ISSUER,
        ).assertErrorType(VcSdJwtError.InvalidVcSdJwt::class)
    }

    @Test
    fun `Verifying a vc sd jwt credential that does not contain a keyId returns an error`() = runTest {
        useCase(
            keyBinding = null,
            payload = JWT_WITHOUT_KID,
        ).assertErrorType(VcSdJwtError.InvalidVcSdJwt::class)
    }

    @Test
    fun `Error from the jwt signature verification are mapped`() = runTest {
        val exception = Exception("invalid signature")
        coEvery {
            mockVerifyJwtSignature(did = any(), kid = any(), jwt = any())
        } returns Err(VcSdJwtError.Unexpected(exception))

        val error = useCase(
            keyBinding = null,
            payload = VALID_JWT,
        ).assertErrorType(VcSdJwtError.Unexpected::class)

        assertEquals(exception, error.cause)
    }

    private companion object {
        const val VALID_JWT =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InR5cGUiLCJraWQiOiJrZXlJZCJ9.eyJpc3MiOiJpc3N1ZXIiLCJzdWIiOiJzdWJqZWN0IiwiZXhwIjoxOTI0OTg4Mzk5LCJpYXQiOjAsIm5iZiI6MSwidmN0IjoidmN0In0.jX5Mfxyh_gJ9VhagwlL80QFZjNgOPgdASjP3awIX-ty_LimDlNDZY3eCpjyecqcKFskkVx55gFs9h8_sENvNyQ"
        const val JWT_WITH_NON_DISCLOSABLE_CLAIM =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InR5cGUiLCJraWQiOiJrZXlJZCJ9.eyJpc3MiOiJpc3N1ZXIiLCJzdWIiOiJzdWJqZWN0IiwiZXhwIjoxOTI0OTg4Mzk5LCJpYXQiOjAsIm5iZiI6MSwidmN0IjoidmN0Iiwib3RoZXIiOiJjbGFpbSJ9.-lrZnzOpojUAL07c1A_4B1UnZZPMB6DkYv5tvsISosizj49wvHr_KMPvsvtZGXP-XkiTto8AG_7VdlgzUASE6w"
        const val JWT_WITHOUT_ISSUER =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InR5cGUiLCJraWQiOiJrZXlJZCJ9.eyJzdWIiOiJzdWJqZWN0IiwiZXhwIjoxOTI0OTg4Mzk5LCJpYXQiOjAsIm5iZiI6MSwidmN0IjoidmN0In0.gBLcxhsnZpKiMZrj-XZrqrTLPktU_pxIRnOvqFCd-fpwAhr6u2rRoVRByejfw7oqyNBRRdiDobK1q7ZZ8WCYOQ"
        const val JWT_WITHOUT_KID =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InR5cGUifQ.eyJpc3MiOiJpc3N1ZXIiLCJzdWIiOiJzdWJqZWN0IiwiZXhwIjoxOTI0OTg4Mzk5LCJpYXQiOjAsIm5iZiI6MSwidmN0IjoidmN0In0.w2aH9-ragocFNC9N6AduOyy0909mFAv3-kSVMZsCmuYB0XHSGxH8_5rsxLLOBYgF0PCsQZbPknyemdpMdgLNQA"
    }
}
