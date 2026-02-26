package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.usecase.VerifyJwtSignature
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class ValidateIssuerMetadataJwtImplTest {

    @MockK
    private lateinit var mockVerifyJwtSignature: VerifyJwtSignature

    private lateinit var useCase: ValidateIssuerMetadataJwtImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = ValidateIssuerMetadataJwtImpl(mockVerifyJwtSignature)
        success()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `UseCase should just run for a valid jwt`() = runTest {
        val result = useCase(MOCK_ISSUER, MOCK_JWT, MOCK_TYPE)

        result.assertOk()
    }

    @Test
    fun `UseCase should just run for a valid jwt without issuer`() = runTest {
        every { MOCK_JWT.iss } returns null

        val result = useCase(MOCK_ISSUER, MOCK_JWT, MOCK_TYPE)

        result.assertOk()
    }

    @Test
    fun `UseCase should just run for a valid jwt without specifying type`() = runTest {
        val result = useCase(MOCK_ISSUER, MOCK_JWT, null)

        result.assertOk()
    }

    @Test
    fun `UseCase should call jwt signature verification`() = runTest {
        useCase(MOCK_ISSUER, MOCK_JWT, MOCK_TYPE)

        coVerify(exactly = 1) {
            mockVerifyJwtSignature(MOCK_DID, MOCK_KEY_IDENTIFIER, MOCK_JWT)
        }
    }

    @Test
    fun `UseCase should return an error when algorithm is unsupported`() = runTest {
        every { MOCK_JWT.algorithm } returns "unsupported"

        val result = useCase(MOCK_ISSUER, MOCK_JWT, MOCK_TYPE)

        result.assertErrorType(CredentialOfferError.InvalidSignedMetadata::class)
    }

    @Test
    fun `UseCase should return an error when type is wrong`() = runTest {
        val result = useCase(MOCK_ISSUER, MOCK_JWT, "other")

        result.assertErrorType(CredentialOfferError.InvalidSignedMetadata::class)
    }

    @Test
    fun `UseCase should return an error when issuedAt is missing`() = runTest {
        every { MOCK_JWT.issuedAt } returns null

        val result = useCase(MOCK_ISSUER, MOCK_JWT, MOCK_TYPE)

        result.assertErrorType(CredentialOfferError.InvalidSignedMetadata::class)
    }

    @Test
    fun `UseCase should return an error when subject is missing`() = runTest {
        every { MOCK_JWT.subject } returns null

        val result = useCase(MOCK_ISSUER, MOCK_JWT, MOCK_TYPE)

        result.assertErrorType(CredentialOfferError.InvalidSignedMetadata::class)
    }

    @Test
    fun `UseCase should return an error when subject is not matching credential issuer identifier`() = runTest {
        val result = useCase("other", MOCK_JWT, MOCK_TYPE)

        result.assertErrorType(CredentialOfferError.InvalidSignedMetadata::class)
    }

    @Test
    fun `UseCase should return an error when issuedAt is in future`() = runTest {
        every { MOCK_JWT.issuedAt } returns Instant.now().plusSeconds(20)

        val result = useCase(MOCK_ISSUER, MOCK_JWT, MOCK_TYPE)

        result.assertErrorType(CredentialOfferError.InvalidSignedMetadata::class)
    }

    @Test
    fun `UseCase should return an error when expired`() = runTest {
        every { MOCK_JWT.expInstant } returns Instant.now().minusSeconds(20)

        val result = useCase(MOCK_ISSUER, MOCK_JWT, MOCK_TYPE)

        result.assertErrorType(CredentialOfferError.InvalidSignedMetadata::class)
    }

    @Test
    fun `UseCase should return an error when no issuer and no did in key identifier`() = runTest {
        every { MOCK_JWT.iss } returns null
        every { MOCK_JWT.keyId } returns "unsupported"

        val result = useCase(MOCK_ISSUER, MOCK_JWT, MOCK_TYPE)

        result.assertErrorType(CredentialOfferError.InvalidSignedMetadata::class)
    }

    @Test
    fun `UseCase should return an error when key identifier is missing`() = runTest {
        every { MOCK_JWT.keyId } returns null

        val result = useCase(MOCK_ISSUER, MOCK_JWT, MOCK_TYPE)

        result.assertErrorType(CredentialOfferError.InvalidSignedMetadata::class)
    }

    @Test
    fun `UseCase should return an error when jwt signature verification fails`() = runTest {
        coEvery {
            mockVerifyJwtSignature(did = MOCK_DID, kid = MOCK_KEY_IDENTIFIER, jwt = MOCK_JWT)
        } returns Err(VcSdJwtError.InvalidJwt)

        val result = useCase(MOCK_ISSUER, MOCK_JWT, MOCK_TYPE)

        result.assertErrorType(CredentialOfferError.InvalidSignedMetadata::class)
    }

    private fun success() {
        every { MOCK_JWT.algorithm } returns "ES256"
        every { MOCK_JWT.type } returns MOCK_TYPE
        every { MOCK_JWT.issuedAt } returns MOCK_INSTANT
        every { MOCK_JWT.subject } returns MOCK_ISSUER
        every { MOCK_JWT.expInstant } returns MOCK_INSTANT.plusSeconds(2)
        every { MOCK_JWT.iss } returns MOCK_DID
        every { MOCK_JWT.keyId } returns MOCK_KEY_IDENTIFIER

        coEvery {
            mockVerifyJwtSignature(did = MOCK_DID, kid = MOCK_KEY_IDENTIFIER, jwt = MOCK_JWT)
        } returns Ok(Unit)
    }

    private companion object {
        const val MOCK_ISSUER = "issuer"
        const val MOCK_DID = "did"
        const val MOCK_KEY_IDENTIFIER = "${MOCK_DID}#keyIdentifier"
        const val MOCK_TYPE = "type"
        val MOCK_JWT = mockk<Jwt>()
        val MOCK_INSTANT: Instant = Instant.now()
    }
}
