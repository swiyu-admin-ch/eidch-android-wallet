package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.didResolver.domain.DidResolverHelper
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.jwt.JwtError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.RequestObject
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.RequestObjectVerificationOutcome
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.usecase.VerifyRequestObjectSignature
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockRequestObject.ATTESTATION_ISSUER_DID
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockRequestObject.ATTESTATION_KEY_ID
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockRequestObject.DID
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockRequestObject.KEY_ID
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockRequestObject.KEY_ID_OTHER
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockRequestObject.attestationRequestObject
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockRequestObject.didRequestObject
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockRequestObject.requestObjectJwtAttestation
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockRequestObject.requestObjectJwtDid
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockRequestObject.requestObjectJwtInvalidDid
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockRequestObject.requestObjectJwtNoClientId
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockRequestObject.requestObjectJwtNoKeyId
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockRequestObject.requestObjectJwtOtherKid
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignature
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignatureFromDid
import ch.admin.foitt.openid4vc.util.SafeJsonTestInstance
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

class VerifyRequestObjectSignatureImplTest {
    @MockK
    private lateinit var mockDidResolverHelper: DidResolverHelper

    @MockK
    private lateinit var mockVerifyJwtSignature: VerifyJwtSignature

    @MockK
    private lateinit var mockVerifyJwtSignatureFromDid: VerifyJwtSignatureFromDid

    private val safeJson = SafeJsonTestInstance.safeJson

    private lateinit var useCase: VerifyRequestObjectSignature

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = VerifyRequestObjectSignatureImpl(
            didResolverHelper = mockDidResolverHelper,
            verifyJwtSignature = mockVerifyJwtSignature,
            verifyJwtSignatureFromDid = mockVerifyJwtSignatureFromDid,
            safeJson = safeJson,
        )

        every { mockDidResolverHelper.getDidStringFromAbsoluteKeyId(KEY_ID) } returns Ok(DID)
        coEvery { mockVerifyJwtSignatureFromDid(kid = KEY_ID, jwt = requestObjectJwtDid) } returns Ok(Unit)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Request object with did client_id is verified successfully`() = runTest {
        val outcome = useCase(didRequestObject, emptyList()).assertOk()
        assertEquals(RequestObjectVerificationOutcome.DID_PATH, outcome)
    }

    @Test
    fun `Request object without client_id returns an error`() = runTest {
        val requestObject = RequestObject(
            clientId = "client_id",
            jwt = requestObjectJwtNoClientId,
            redirectUri = null,
        )

        useCase(requestObject, emptyList()).assertErrorType(VcSdJwtError.Unexpected::class)
    }

    @Test
    fun `Request object with invalid did as client_id returns an error`() = runTest {
        val requestObject = RequestObject(
            clientId = "client_id",
            jwt = requestObjectJwtInvalidDid,
            redirectUri = null,
        )

        useCase(requestObject, emptyList()).assertErrorType(VcSdJwtError.InvalidRequestObject::class)
    }

    @Test
    fun `Request object with did client_id but without keyId returns an error`() = runTest {
        val requestObject = RequestObject(
            clientId = "client_id",
            jwt = requestObjectJwtNoKeyId,
            redirectUri = null,
        )

        useCase(requestObject, emptyList()).assertErrorType(VcSdJwtError.Unexpected::class)
    }

    @Test
    fun `Request object with invalid did as keyId returns an error`() = runTest {
        val exception = IllegalStateException("did error")
        every { mockDidResolverHelper.getDidStringFromAbsoluteKeyId(KEY_ID) } returns Err(exception)

        useCase(didRequestObject, emptyList()).assertErrorType(VcSdJwtError.Unexpected::class)
    }

    @Test
    fun `Request object where client_id does not match kid returns an error`() = runTest {
        every { mockDidResolverHelper.getDidStringFromAbsoluteKeyId(KEY_ID_OTHER) } returns Ok("other")

        val requestObject = RequestObject(
            clientId = "client_id",
            jwt = requestObjectJwtOtherKid,
            redirectUri = null,
        )

        useCase(requestObject, emptyList()).assertErrorType(VcSdJwtError.InvalidRequestObject::class)
    }

    @Test
    fun `Request object with did client_id maps errors from jwt signature validation`() = runTest {
        val exception = IllegalStateException("jwt signature failure")
        coEvery {
            mockVerifyJwtSignatureFromDid(kid = KEY_ID, jwt = requestObjectJwtDid)
        } returns Err(JwtError.Unexpected(exception))

        useCase(didRequestObject, emptyList()).assertErrorType(VcSdJwtError.Unexpected::class)
    }

    @Test
    fun `Request object with verifierAttestation client_id is verified as ATTESTATION_TRUSTED when issuer DID is trusted`() = runTest {
        every { mockDidResolverHelper.getDidStringFromAbsoluteKeyId(ATTESTATION_KEY_ID) } returns Ok(ATTESTATION_ISSUER_DID)
        coEvery { mockVerifyJwtSignatureFromDid(kid = ATTESTATION_KEY_ID, jwt = any()) } returns Ok(Unit)
        coEvery { mockVerifyJwtSignature(jwt = requestObjectJwtAttestation, publicKey = any<Jwk>()) } returns Ok(Unit)

        val outcome = useCase(attestationRequestObject, listOf(ATTESTATION_ISSUER_DID)).assertOk()
        assertEquals(RequestObjectVerificationOutcome.ATTESTATION_TRUSTED, outcome)
    }

    @Test
    fun `Request object with verifierAttestation client_id is verified as ATTESTATION_UNTRUSTED when issuer DID is not trusted`() = runTest {
        every { mockDidResolverHelper.getDidStringFromAbsoluteKeyId(ATTESTATION_KEY_ID) } returns Ok(ATTESTATION_ISSUER_DID)
        coEvery { mockVerifyJwtSignatureFromDid(kid = ATTESTATION_KEY_ID, jwt = any()) } returns Ok(Unit)
        coEvery { mockVerifyJwtSignature(jwt = requestObjectJwtAttestation, publicKey = any<Jwk>()) } returns Ok(Unit)

        val outcome = useCase(attestationRequestObject, emptyList()).assertOk()
        assertEquals(RequestObjectVerificationOutcome.ATTESTATION_UNTRUSTED, outcome)
    }
}
