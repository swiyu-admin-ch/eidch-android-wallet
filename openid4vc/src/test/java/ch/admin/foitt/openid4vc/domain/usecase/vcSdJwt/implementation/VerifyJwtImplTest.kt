package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.eid.didresolver.did_sidekicks.DidDoc
import ch.admin.eid.didresolver.did_sidekicks.Jwk
import ch.admin.eid.didresolver.did_sidekicks.VerificationMethod
import ch.admin.eid.didresolver.did_sidekicks.VerificationType
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.usecase.ResolveDid
import ch.admin.foitt.openid4vc.domain.usecase.implementation.VerifyJwtSignatureImpl
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.VerifyPublicKey
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VerifyJwtImplTest {
    @MockK
    private lateinit var mockResolveDid: ResolveDid

    @MockK
    private lateinit var mockJwt: Jwt

    @MockK
    private lateinit var mockSignedJWT: SignedJWT

    @MockK
    private lateinit var mockDidDoc: DidDoc

    @MockK
    private lateinit var mockVerifyPublicKey: VerifyPublicKey

    private lateinit var useCase: VerifyJwtSignatureImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { mockJwt.signedJwt } returns mockSignedJWT
        coEvery { mockResolveDid.invoke(any()) } returns Ok(mockDidDoc)
        every { mockDidDoc.getDeactivated() } returns false
        every { mockDidDoc.getVerificationMethod() } returns mockVerificationMethods
        every { mockVerifyPublicKey(any(), any()) } returns Ok(Unit)

        useCase = VerifyJwtSignatureImpl(
            resolveDid = mockResolveDid,
            verifyPublicKey = mockVerifyPublicKey
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Verifying jwt which is valid returns Ok`(): Unit = runTest {
        val result = useCase(
            did = ISSUER_ID1,
            kid = KID_ID1,
            jwt = mockJwt,
        )

        result.assertOk()
    }

    @Test
    fun `Verifying jwt which has one matching public key from list returns Ok`(): Unit = runTest {
        every { mockDidDoc.getVerificationMethod() } returns listOf(mockVerificationMethod1, mockVerificationMethod2)
        every { mockVerifyPublicKey(mockJwk1, any()) } returns Ok(Unit)
        every { mockVerifyPublicKey(mockJwk2, any()) } returns Err(Unit)

        val result = useCase(did = ISSUER_ID1, kid = KID_ID1, jwt = mockJwt)

        result.assertOk()
    }

    @Test
    fun `Verifying jwt which has no matching public key from list returns InvalidJwt`(): Unit = runTest {
        every { mockDidDoc.getVerificationMethod() } returns listOf(mockVerificationMethod2, mockVerificationMethod3)
        every { mockVerifyPublicKey(mockJwk1, any()) } returns Err(Unit)
        every { mockVerifyPublicKey(mockJwk2, any()) } returns Err(Unit)

        val result = useCase(did = ISSUER_ID1, kid = KID_ID1, jwt = mockJwt)

        result.assertErrorType(VcSdJwtError.InvalidJwt::class)
    }

    @Test
    fun `Verifying jwt with only null jwk keys returns InvalidJwt`(): Unit = runTest {
        every { mockDidDoc.getVerificationMethod() } returns listOf(mockVerificationMethod3)

        val result = useCase(did = ISSUER_ID1, kid = "", jwt = mockJwt)

        result.assertErrorType(VcSdJwtError.InvalidJwt::class)
    }

    @Test
    fun `Verifying jwt with empty public key list returns InvalidJwt`(): Unit = runTest {
        every { mockDidDoc.getVerificationMethod() } returns listOf()

        val result = useCase(did = ISSUER_ID1, kid = "", jwt = mockJwt)

        result.assertErrorType(VcSdJwtError.InvalidJwt::class)
    }

    @Test
    fun `Verifying jwt uses correct public key from list`(): Unit = runTest {
        val result = useCase(did = ISSUER_ID1, kid = KID_ID2, jwt = mockJwt)

        result.assertOk()
    }

    @Test
    fun `Verifying jwt with didDoc deactivated returns DidDocumentDeactivated`(): Unit = runTest {
        every { mockDidDoc.getDeactivated() } returns true

        val result = useCase(did = ISSUER_ID1, kid = KID_ID1, jwt = mockJwt)

        result.assertErrorType(VcSdJwtError.DidDocumentDeactivated::class)
    }

    companion object {
        private const val ISSUER_ID1 = "issuer1"
        private const val KID_ID1 = "kid1"
        private val mockJwk1 = Jwk(
            alg = "alg1",
            kid = KID_ID1,
            kty = "kty1",
            crv = "crv1",
            x = "x1",
            y = "y1",
        )

        private val mockVerificationMethod1 = VerificationMethod(
            id = KID_ID1,
            verificationType = VerificationType.ED25519_VERIFICATION_KEY2020,
            controller = "controller",
            publicKeyJwk = mockJwk1,
            publicKeyMultibase = "multibase",
        )

        private const val KID_ID2 = "issuer2#kid2"
        private val mockJwk2 = mockJwk1.copy(kid = KID_ID2)
        private val mockVerificationMethod2 = VerificationMethod(
            id = KID_ID2,
            verificationType = VerificationType.ED25519_VERIFICATION_KEY2020,
            controller = "controller",
            publicKeyJwk = mockJwk2,
            publicKeyMultibase = "multibase",
        )

        private val mockVerificationMethod3 = mockVerificationMethod1.copy(id = "id3", publicKeyJwk = null)

        private val mockVerificationMethods = listOf(
            mockVerificationMethod1,
            mockVerificationMethod2,
            mockVerificationMethod3,
            mockVerificationMethod1,
        )
    }
}
