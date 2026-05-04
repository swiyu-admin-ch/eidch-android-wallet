package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.foitt.openid4vc.domain.model.jwt.JwtError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignatureFromDid
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
    private lateinit var mockVerifyJwtSignatureFromDid: VerifyJwtSignatureFromDid

    private lateinit var useCase: VerifyVcSdJwtSignatureImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = VerifyVcSdJwtSignatureImpl(
            verifyJwtSignatureFromDid = mockVerifyJwtSignatureFromDid,
        )

        coEvery {
            mockVerifyJwtSignatureFromDid(did = any(), kid = any(), jwt = any())
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
    fun `Verifying a vc sd jwt credential that contains non-selectively disclosable claims returns an error`() = runTest {
        useCase(
            keyBinding = null,
            payload = JWT_WITH_NON_SELECTIVELY_DISCLOSABLE_CLAIM,
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
            mockVerifyJwtSignatureFromDid(did = any(), kid = any(), jwt = any())
        } returns Err(JwtError.Unexpected(exception))

        val error = useCase(
            keyBinding = null,
            payload = VALID_JWT,
        ).assertErrorType(VcSdJwtError.Unexpected::class)

        assertEquals(exception, error.cause)
    }

    private companion object {
        const val VALID_JWT =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InR5cGUiLCJraWQiOiJrZXlJZCJ9.eyJpc3MiOiJpc3N1ZXIiLCJleHAiOjE5MjQ5ODgzOTksImlhdCI6MCwibmJmIjoxLCJ2Y3QiOiJ2Y3QifQ.xHItSO9jil0yiltXr0WFVGxiogOsihsfX0k5INgcoC9k4oP69yTM_mNqujBM5DB0_x__ZXQF9Sc_1GU__5wZdg~"
        const val JWT_WITH_NON_SELECTIVELY_DISCLOSABLE_CLAIM =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InR5cGUiLCJraWQiOiJrZXlJZCJ9.eyJpc3MiOiJpc3N1ZXIiLCJleHAiOjE5MjQ5ODgzOTksImlhdCI6MCwibmJmIjoxLCJ2Y3QiOiJ2Y3QiLCJvdGhlciI6ImNsYWltIn0.B0OsUc5CukjhaBChyDrLJdx8paChpV3ghZxDMtn7bY1JT3IQrLu1I1WapetEE9_XgRJn9exz9Ms_HGLT1pDh6g~"
        const val JWT_WITHOUT_ISSUER =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InR5cGUiLCJraWQiOiJrZXlJZCJ9.eyJleHAiOjE5MjQ5ODgzOTksImlhdCI6MCwibmJmIjoxLCJ2Y3QiOiJ2Y3QifQ.TqCN6faq5ZX7QYYW6fsdthVxnzv9t7d4U9U_g8TWtJt5Sm-HKPmqFFLLC0G8xhINNWvIbMVZ7cat84nEmlLAqw~"
        const val JWT_WITHOUT_KID =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InR5cGUifQ.eyJpc3MiOiJpc3N1ZXIiLCJleHAiOjE5MjQ5ODgzOTksImlhdCI6MCwibmJmIjoxLCJ2Y3QiOiJ2Y3QifQ.VarWJBHA1ABRVYJOKxB3Vg_PFc6iGuAtCx20XrwRRaULoSLHnmmdhu3RrS2nfMCzg6ZpiQ-3krCwAgsqFuX45A~"
    }
}
