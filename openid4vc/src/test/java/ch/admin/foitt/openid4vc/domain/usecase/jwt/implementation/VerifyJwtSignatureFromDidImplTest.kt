package ch.admin.foitt.openid4vc.domain.usecase.jwt.implementation

import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.jwt.JwtError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.usecase.ResolvePublicKey
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignature
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignatureFromDid
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VerifyJwtSignatureFromDidImplTest {

    @MockK
    private lateinit var mockResolvePublicKey: ResolvePublicKey

    @MockK
    private lateinit var mockVerifyJwtSignature: VerifyJwtSignature

    @MockK
    private lateinit var mockJwt: Jwt

    @MockK
    private lateinit var mockJwk: Jwk

    private lateinit var useCase: VerifyJwtSignatureFromDid

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = VerifyJwtSignatureFromDidImpl(
            resolvePublicKey = mockResolvePublicKey,
            verifyJwtSignature = mockVerifyJwtSignature,
        )

        coEvery {
            mockResolvePublicKey(did = DID, kid = KID)
        } returns Ok(mockJwk)

        coEvery {
            mockVerifyJwtSignature(jwt = mockJwt, publicKey = mockJwk)
        } returns Ok(Unit)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Verifying a did that matches the signature of the jwt returns a success`() = runTest {
        useCase(DID, KID, mockJwt).assertOk()
    }

    @Test
    fun `VerifyJwtSignature maps unexpected errors from resolving the public key`() = runTest {
        val exception = IllegalStateException("public key error")
        coEvery {
            mockResolvePublicKey(DID, KID)
        } returns Err(VcSdJwtError.Unexpected(exception))

        useCase(DID, KID, mockJwt).assertErrorType(JwtError.Unexpected::class)
    }

    @Test
    fun `VerifyJwtSignature maps validation errors from ResolveDid`() = runTest {
        val exception = IllegalStateException("jwt signature error")
        coEvery {
            mockVerifyJwtSignature(mockJwt, mockJwk)
        } returns Err(JwtError.Unexpected(exception))

        useCase(DID, KID, mockJwt).assertErrorType(JwtError.Unexpected::class)
    }

    private companion object {
        const val DID = "did"
        const val KID = "keyIdentifier"
    }
}
