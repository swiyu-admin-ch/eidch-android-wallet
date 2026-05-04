package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.eid.didresolver.did_sidekicks.DidDoc
import ch.admin.eid.didresolver.did_sidekicks.Jwk
import ch.admin.eid.didresolver.did_sidekicks.VerificationMethod
import ch.admin.foitt.openid4vc.domain.model.ResolveDidError
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.usecase.ResolveDid
import ch.admin.foitt.openid4vc.domain.usecase.ResolvePublicKey
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.nimbusds.jwt.SignedJWT
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

class ResolvePublicKeyImplTest {

    @MockK
    private lateinit var mockResolveDid: ResolveDid

    @MockK
    private lateinit var mockJwt: Jwt

    @MockK
    private lateinit var mockSignedJwt: SignedJWT

    @MockK
    private lateinit var mockDidDoc: DidDoc

    @MockK
    private lateinit var mockVerificationMethod1: VerificationMethod

    @MockK
    private lateinit var mockDidJwk1: Jwk

    @MockK
    private lateinit var mockVerificationMethod2: VerificationMethod

    @MockK
    private lateinit var mockDidJwk2: Jwk
    private lateinit var useCase: ResolvePublicKey

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = ResolvePublicKeyImpl(
            resolveDid = mockResolveDid,
        )

        coEvery { mockResolveDid(DID) } returns Ok(mockDidDoc)
        every { mockDidDoc.getDeactivated() } returns false
        every { mockDidDoc.getVerificationMethod() } returns listOf(mockVerificationMethod1, mockVerificationMethod2)
        every { mockVerificationMethod1.id } returns KID1
        every { mockVerificationMethod1.publicKeyJwk } returns mockDidJwk1
        every { mockVerificationMethod2.id } returns KID2
        every { mockVerificationMethod2.publicKeyJwk } returns mockDidJwk2
        every { mockJwt.signedJwt } returns mockSignedJwt

        every { mockDidJwk1.x } returns X_VALUE
        every { mockDidJwk1.y } returns Y_VALUE
        every { mockDidJwk1.crv } returns CRV
        every { mockDidJwk1.kty } returns KTY

        every { mockDidJwk2.x } returns X_VALUE_2
        every { mockDidJwk2.y } returns Y_VALUE_2
        every { mockDidJwk2.crv } returns CRV_2
        every { mockDidJwk2.kty } returns KTY_2
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Resolving public key from did returns a success`() = runTest {
        val result = useCase(DID, KID1).assertOk()

        val expected = ch.admin.foitt.openid4vc.domain.model.jwk.Jwk(
            x = X_VALUE,
            y = Y_VALUE,
            crv = CRV,
            kty = KTY,
        )

        assertEquals(expected, result)
    }

    @Test
    fun `Resolving public key maps errors from ResolveDid`() = runTest {
        val exception = Exception("Did exception")
        coEvery { mockResolveDid(DID) } returns Err(ResolveDidError.Unexpected(exception))

        useCase(DID, KID1).assertErrorType(VcSdJwtError.Unexpected::class)
    }

    @Test
    fun `Resolving public key maps validation errors from ResolveDid`() = runTest {
        coEvery { mockResolveDid(DID) } returns Err(ResolveDidError.ValidationFailure)

        useCase(DID, KID1).assertErrorType(VcSdJwtError.IssuerValidationFailed::class)
    }

    @Test
    fun `Resolving public key with deactivated did document returns an error`() = runTest {
        every { mockDidDoc.getDeactivated() } returns true

        useCase(DID, KID1).assertErrorType(VcSdJwtError.DidDocumentDeactivated::class)
    }

    @Test
    fun `Resolving public key takes the first verification method matching the kid`() = runTest {
        every { mockVerificationMethod1.id } returns KID1
        every { mockVerificationMethod2.id } returns KID1

        val result = useCase(DID, KID1).assertOk()

        assertEquals(X_VALUE, result.x)
        assertEquals(Y_VALUE, result.y)
        assertEquals(CRV, result.crv)
        assertEquals(KTY, result.kty)
    }

    @Test
    fun `Resolving public key takes the verification method matching the kid`() = runTest {
        val result = useCase(DID, KID2).assertOk()

        assertEquals(X_VALUE_2, result.x)
        assertEquals(Y_VALUE_2, result.y)
        assertEquals(CRV_2, result.crv)
        assertEquals(KTY_2, result.kty)
    }

    @Test
    fun `Resolving public key returns an error if no verification method is found for the provided kid`() = runTest {
        useCase(DID, "non-matching kid").assertErrorType(VcSdJwtError.InvalidJwt::class)
    }

    @Test
    fun `Resolving public key returns an error if the publicKeyJwk of the matching verification method is null`() = runTest {
        every { mockVerificationMethod1.publicKeyJwk } returns null

        useCase(DID, KID1).assertErrorType(VcSdJwtError.InvalidJwt::class)
    }

    private companion object {
        const val DID = "did"
        const val KID1 = "keyIdentifier1"
        const val KID2 = "keyIdentifier2"
        const val X_VALUE = "x"
        const val Y_VALUE = "y"
        const val CRV = "crv"
        const val KTY = "kty"
        const val X_VALUE_2 = "x2"
        const val Y_VALUE_2 = "y2"
        const val CRV_2 = "crv2"
        const val KTY_2 = "kty2"
    }
}
