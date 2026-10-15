package ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.implementation

import ch.admin.foitt.didResolver.domain.DidResolverHelper
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.usecase.ResolvePublicKey
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignature
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.utils.SafeJson
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.VersionEnforcementError
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VerifyDetachedJwsImplTest {

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    @MockK
    private lateinit var mockDidResolverHelper: DidResolverHelper

    @MockK
    private lateinit var mockResolvePublicKey: ResolvePublicKey

    @MockK
    private lateinit var mockVerifyJwtSignature: VerifyJwtSignature

    private lateinit var useCase: VerifyDetachedJwsImpl

    private val json = Json { ignoreUnknownKeys = true }
    private val safeJson = SafeJson(json)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = VerifyDetachedJwsImpl(
            environmentSetupRepository = mockEnvironmentSetupRepository,
            didResolverHelper = mockDidResolverHelper,
            resolvePublicKey = mockResolvePublicKey,
            verifyJwtSignature = mockVerifyJwtSignature,
            safeJson = safeJson,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Successfully verifying a detached JWS returns Ok`() = runTest {
        every { mockEnvironmentSetupRepository.versionEnforcementTrustedDids } returns listOf(DID)
        every { mockDidResolverHelper.getDidStringFromAbsoluteKeyId(KID) } returns Ok(DID)
        coEvery { mockResolvePublicKey(KID) } returns Ok(JWK)
        every { mockVerifyJwtSignature.invoke(any(), any(), any(), any()) } returns Ok(Unit)

        useCase(jws = VALID_JWS, body = BODY).assertOk()
    }

    @Test
    fun `Verifying a detached JWS with invalid format returns error`() = runTest {
        useCase(jws = "invalid.jws", body = BODY).assertErrorType(VersionEnforcementError.SignatureValidationError::class)
    }

    @Test
    fun `Verifying a detached JWS with non-empty payload returns error`() = runTest {
        val jwsWithPayload = "eyJhbGciOiJFUzI1NiIsImI2NCI6ZmFsc2UsImNyaXQiOlsiYjY0Il0sImtpZCI6ImtpZCJ9.cGF5bG9hZA.signature"
        useCase(jws = jwsWithPayload, body = BODY).assertErrorType(VersionEnforcementError.SignatureValidationError::class)
    }

    @Test
    fun `Verifying a detached JWS with b64 not false returns error`() = runTest {
        // {"alg":"ES256","b64":true,"crit":["b64"],"kid":"kid"}
        val jwsB64True = "eyJhbGciOiJFUzI1NiIsImI2NCI6dHJ1ZSwiY3JpdCI6WyJiNjQiXSwia2lkIjoia2lkIn0..signature"
        useCase(jws = jwsB64True, body = BODY).assertErrorType(VersionEnforcementError.SignatureValidationError::class)
    }

    @Test
    fun `Verifying a detached JWS with missing crit b64 returns error`() = runTest {
        // {"alg":"ES256","b64":false,"kid":"kid"}
        val jwsMissingCrit = "eyJhbGciOiJFUzI1NiIsImI2NCI6ZmFsc2UsImtpZCI6ImtpZCJ9..signature"
        useCase(jws = jwsMissingCrit, body = BODY).assertErrorType(VersionEnforcementError.SignatureValidationError::class)
    }

    @Test
    fun `Verifying a detached JWS with unsupported algorithm returns error`() = runTest {
        // {"alg":"RS256","b64":false,"crit":["b64"],"kid":"kid"}
        val jwsRS256 = "eyJhbGciOiJSUzI1NiIsImI2NCI6ZmFsc2UsImNyaXQiOlsiYjY0Il0sImtpZCI6ImtpZCJ9..signature"
        useCase(jws = jwsRS256, body = BODY).assertErrorType(VersionEnforcementError.SignatureValidationError::class)
    }

    @Test
    fun `Verifying a detached JWS with untrusted DID returns error`() = runTest {
        every { mockEnvironmentSetupRepository.versionEnforcementTrustedDids } returns listOf("other-did")
        every { mockDidResolverHelper.getDidStringFromAbsoluteKeyId(KID) } returns Ok(DID)

        useCase(jws = VALID_JWS, body = BODY).assertErrorType(VersionEnforcementError.SignatureValidationError::class)
    }

    @Test
    fun `Verifying a detached JWS with failed public key resolution returns error`() = runTest {
        every { mockEnvironmentSetupRepository.versionEnforcementTrustedDids } returns listOf(DID)
        every { mockDidResolverHelper.getDidStringFromAbsoluteKeyId(KID) } returns Ok(DID)
        coEvery {
            mockResolvePublicKey(KID)
        } returns Err(ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError.Unexpected(null))

        useCase(jws = VALID_JWS, body = BODY).assertErrorType(VersionEnforcementError.SignatureValidationError::class)
    }

    @Test
    fun `Verifying a detached JWS with invalid signature returns error`() = runTest {
        every { mockEnvironmentSetupRepository.versionEnforcementTrustedDids } returns listOf(DID)
        every { mockDidResolverHelper.getDidStringFromAbsoluteKeyId(KID) } returns Ok(DID)
        coEvery { mockResolvePublicKey(KID) } returns Ok(JWK)
        every {
            mockVerifyJwtSignature.invoke(any(), any(), any(), any())
        } returns Err(ch.admin.foitt.openid4vc.domain.model.jwt.JwtError.InvalidJwt)

        useCase(jws = VALID_JWS, body = BODY).assertErrorType(VersionEnforcementError.SignatureValidationError::class)
    }

    private companion object {
        const val DID = "did:tdw:QmVF...:api:v1:did:9b1bdb98-461f-4ce2-80e0-94cf3a04ac83"
        const val KID = "$DID#version-assertion-1"
        const val BODY = "{\"foo\":\"bar\"}"

        // {"alg":"ES256","b64":false,"crit":["b64"],"kid":"did:tdw:QmVF...:api:v1:did:9b1bdb98-461f-4ce2-80e0-94cf3a04ac83#version-assertion-1"}
        const val VALID_JWS = "eyJhbGciOiJFUzI1NiIsImI2NCI6ZmFsc2UsImNyaXQiOlsiYjY0Il0sImtpZCI6ImRpZDp0ZHc6UW1WRi4uLjphcGk6djE6ZGlkOjliMWJkYjk4LTQ2MWYtNGNlMi04MGUwLTk0Y2YzYTA0YWM4MyN2ZXJzaW9uLWFzc2VydGlvbi0xIn0..signature"

        val JWK = Jwk(
            x = "x",
            y = "y",
            crv = "P-256",
            kty = "EC",
            kid = KID
        )
    }
}
