package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.jwt.JwtError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.RequestObject
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.usecase.VerifyRequestObjectSignature
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockRequestObject.DID
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockRequestObject.KEY_ID_DID
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockRequestObject.didRequestObject
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
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VerifyRequestObjectSignatureImplTest {
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
            verifyJwtSignature = mockVerifyJwtSignature,
            verifyJwtSignatureFromDid = mockVerifyJwtSignatureFromDid,
            safeJson = safeJson,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Request object with did client_id is verified successfully`() = runTest {
        setupDidDefaultMocks()

        useCase(didRequestObject, emptyList()).assertOk()
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
    fun `Request object with did client_id but without keyId returns an error`() = runTest {
        val requestObject = RequestObject(
            clientId = "client_id",
            jwt = requestObjectJwtNoKeyId,
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
    fun `Request object where client_id does not match kid returns an error`() = runTest {
        val requestObject = RequestObject(
            clientId = "client_id",
            jwt = requestObjectJwtOtherKid,
            redirectUri = null,
        )

        useCase(requestObject, emptyList()).assertErrorType(VcSdJwtError.InvalidRequestObject::class)
    }

    @Test
    fun `Request object with did client_id maps errors from jwt signature validation`() = runTest {
        setupDidDefaultMocks()
        val exception = IllegalStateException("jwt signature failure")
        coEvery {
            mockVerifyJwtSignatureFromDid(did = DID, kid = KEY_ID_DID, jwt = requestObjectJwtDid)
        } returns Err(JwtError.Unexpected(exception))

        val requestObject = RequestObject(
            clientId = "client_id",
            jwt = requestObjectJwtDid,
            redirectUri = null,
        )

        useCase(requestObject, emptyList()).assertErrorType(VcSdJwtError.Unexpected::class)
    }

    @Test
    fun `Request object with verifierAttestation client_id is verified successfully`() = runTest {
    }

    private fun setupDidDefaultMocks() {
        coEvery { mockVerifyJwtSignatureFromDid(did = DID, kid = KEY_ID_DID, jwt = requestObjectJwtDid) } returns Ok(Unit)
    }
}
