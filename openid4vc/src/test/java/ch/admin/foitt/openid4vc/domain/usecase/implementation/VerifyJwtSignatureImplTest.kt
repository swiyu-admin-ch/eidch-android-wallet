package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.eid.didresolver.did_sidekicks.DidDoc
import ch.admin.eid.didresolver.did_sidekicks.Jwk
import ch.admin.eid.didresolver.did_sidekicks.VerificationMethod
import ch.admin.foitt.openid4vc.domain.model.ResolveDidError
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.usecase.ResolveDid
import ch.admin.foitt.openid4vc.domain.usecase.VerifyJwtSignature
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.VerifyPublicKey
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.nimbusds.jwt.SignedJWT
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

class VerifyJwtSignatureImplTest {

    @MockK
    private lateinit var mockResolveDid: ResolveDid

    @MockK
    private lateinit var mockVerifyPublicKey: VerifyPublicKey

    @MockK
    private lateinit var mockJwt: Jwt

    @MockK
    private lateinit var mockSignedJwt: SignedJWT

    @MockK
    private lateinit var mockDidDoc: DidDoc

    @MockK
    private lateinit var mockVerificationMethod1: VerificationMethod

    @MockK
    private lateinit var mockJwk1: Jwk

    @MockK
    private lateinit var mockVerificationMethod2: VerificationMethod

    @MockK
    private lateinit var mockJwk2: Jwk

    private lateinit var useCase: VerifyJwtSignature

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = VerifyJwtSignatureImpl(
            resolveDid = mockResolveDid,
            verifyPublicKey = mockVerifyPublicKey,
        )

        coEvery { mockResolveDid(DID) } returns Ok(mockDidDoc)
        every { mockDidDoc.getDeactivated() } returns false
        every { mockDidDoc.getVerificationMethod() } returns listOf(mockVerificationMethod1, mockVerificationMethod2)
        every { mockVerificationMethod1.id } returns KID1
        every { mockVerificationMethod1.publicKeyJwk } returns mockJwk1
        every { mockVerificationMethod2.id } returns KID2
        every { mockVerificationMethod2.publicKeyJwk } returns mockJwk2
        every { mockJwt.signedJwt } returns mockSignedJwt
        coEvery { mockVerifyPublicKey(mockJwk1, mockSignedJwt) } returns Ok(Unit)
        coEvery { mockVerifyPublicKey(mockJwk2, mockSignedJwt) } returns Ok(Unit)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Verifying a did that matches the signature of the jwt returns a success`() = runTest {
        useCase(DID, KID1, mockJwt).assertOk()
    }

    @Test
    fun `VerifyJwtSignature maps unexpected errors from ResolveDid`() = runTest {
        val didUnexpectedError = ResolveDidError.Unexpected(Exception("Did exception"))
        coEvery { mockResolveDid(DID) } returns Err(didUnexpectedError)

        val error = useCase(DID, KID1, mockJwt).assertErrorType(VcSdJwtError.Unexpected::class)
        assertEquals(didUnexpectedError.cause, error.cause)
    }

    @Test
    fun `VerifyJwtSignature maps validation errors from ResolveDid`() = runTest {
        coEvery { mockResolveDid(DID) } returns Err(ResolveDidError.ValidationFailure)

        useCase(DID, KID1, mockJwt).assertErrorType(VcSdJwtError.IssuerValidationFailed::class)
    }

    @Test
    fun `VerifyJwtSignature takes the first verification method matching the kid`() = runTest {
        every { mockVerificationMethod1.id } returns KID1
        every { mockVerificationMethod2.id } returns KID1

        useCase(DID, KID1, mockJwt).assertOk()

        coVerify { mockVerifyPublicKey(mockJwk1, mockSignedJwt) }
    }

    @Test
    fun `VerifyJwtSignature takes the verification method matching the kid`() = runTest {
        useCase(DID, KID2, mockJwt).assertOk()

        coVerify { mockVerifyPublicKey(mockJwk2, mockSignedJwt) }
    }

    @Test
    fun `VerifyJwtSignature returns an error if no verification method is found for the provided kid`() = runTest {
        useCase(DID, "non-matching kid", mockJwt).assertErrorType(VcSdJwtError.InvalidJwt::class)
    }

    @Test
    fun `VerifyJwtSignature returns an error if the publicKeyJwk of the matching verification method is null`() = runTest {
        every { mockVerificationMethod1.publicKeyJwk } returns null

        useCase(DID, KID1, mockJwt).assertErrorType(VcSdJwtError.InvalidJwt::class)
    }

    @Test
    fun `VerifyJwtSignature returns an error when the signatures don't match`() = runTest {
        coEvery { mockVerifyPublicKey(mockJwk1, mockSignedJwt) } returns Err(Unit)

        useCase(DID, KID1, mockJwt).assertErrorType(VcSdJwtError.InvalidJwt::class)
    }

    private companion object {
        const val DID = "did"
        const val KID1 = "keyIdentifier1"
        const val KID2 = "keyIdentifier2"
    }
}
