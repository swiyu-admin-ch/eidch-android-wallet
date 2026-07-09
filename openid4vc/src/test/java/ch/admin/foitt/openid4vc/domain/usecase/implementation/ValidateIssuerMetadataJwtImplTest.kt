package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.Validity
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.jwt.JwtError
import ch.admin.foitt.openid4vc.domain.usecase.ValidateIssuerMetadataJwt
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignatureFromDid
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class ValidateIssuerMetadataJwtImplTest {

    @MockK
    private lateinit var mockVerifyJwtSignatureFromDid: VerifyJwtSignatureFromDid

    @MockK
    private lateinit var jwt: Jwt

    private lateinit var useCase: ValidateIssuerMetadataJwt

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = ValidateIssuerMetadataJwtImpl(mockVerifyJwtSignatureFromDid)
        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `UseCase should just run for a valid jwt`() = runTest {
        useCase(ISSUER, jwt, TYPE).assertOk()

        coVerify(exactly = 1) {
            mockVerifyJwtSignatureFromDid(KEY_ID, jwt)
        }
    }

    @Test
    fun `UseCase should just run for a valid jwt without specifying type`() = runTest {
        useCase(ISSUER, jwt, null).assertOk()

        coVerify(exactly = 1) {
            mockVerifyJwtSignatureFromDid(KEY_ID, jwt)
        }
    }

    @Test
    fun `UseCase should return an error when algorithm is unsupported`() = runTest {
        every { jwt.algorithm } returns "unsupported"

        useCase(ISSUER, jwt, TYPE).assertErrorType(CredentialOfferError.InvalidSignedMetadata::class)
    }

    @Test
    fun `UseCase should return an error when type is wrong`() = runTest {
        useCase(ISSUER, jwt, "otherType").assertErrorType(CredentialOfferError.InvalidSignedMetadata::class)

        every { jwt.type } returns "otherType"
        useCase(ISSUER, jwt, TYPE).assertErrorType(CredentialOfferError.InvalidSignedMetadata::class)
    }

    @Test
    fun `UseCase should return an error when issuedAt is missing`() = runTest {
        every { jwt.issuedAt } returns null

        useCase(ISSUER, jwt, TYPE).assertErrorType(CredentialOfferError.InvalidSignedMetadata::class)
    }

    @Test
    fun `UseCase should return an error when subject is missing`() = runTest {
        every { jwt.subject } returns null

        useCase(ISSUER, jwt, TYPE).assertErrorType(CredentialOfferError.InvalidSignedMetadata::class)
    }

    @Test
    fun `UseCase should return an error when subject is not matching credential issuer identifier`() = runTest {
        useCase("otherIssuer", jwt, TYPE).assertErrorType(CredentialOfferError.InvalidSignedMetadata::class)

        every { jwt.subject } returns "otherSubject"

        useCase(ISSUER, jwt, TYPE).assertErrorType(CredentialOfferError.InvalidSignedMetadata::class)
    }

    @Test
    fun `UseCase should return an error when jwt is not valid`() = runTest {
        every { jwt.jwtValidity } returns Validity.Expired(Instant.now())

        useCase(ISSUER, jwt, TYPE).assertErrorType(CredentialOfferError.InvalidSignedMetadata::class)
    }

    @Test
    fun `UseCase should return an error when key identifier is missing`() = runTest {
        every { jwt.keyId } returns null

        useCase(ISSUER, jwt, TYPE).assertErrorType(CredentialOfferError.InvalidSignedMetadata::class)
    }

    @Test
    fun `UseCase should return an error when jwt signature verification fails`() = runTest {
        coEvery {
            mockVerifyJwtSignatureFromDid(kid = KEY_ID, jwt = jwt)
        } returns Err(JwtError.InvalidJwt)

        useCase(ISSUER, jwt, TYPE).assertErrorType(CredentialOfferError.InvalidSignedMetadata::class)
    }

    private fun setupDefaultMocks() {
        every { jwt.algorithm } returns "ES256"
        every { jwt.type } returns TYPE
        every { jwt.issuedAt } returns MOCK_INSTANT
        every { jwt.subject } returns ISSUER
        every { jwt.expInstant } returns MOCK_INSTANT.plusSeconds(2)
        every { jwt.keyId } returns KEY_ID
        every { jwt.jwtValidity } returns Validity.Valid

        coEvery {
            mockVerifyJwtSignatureFromDid(kid = KEY_ID, jwt = jwt)
        } returns Ok(Unit)
    }

    private companion object {
        const val ISSUER = "issuer"
        const val KEY_ID = "$ISSUER#key-01"
        const val TYPE = "type"
        val MOCK_INSTANT: Instant = Instant.now()
    }
}
