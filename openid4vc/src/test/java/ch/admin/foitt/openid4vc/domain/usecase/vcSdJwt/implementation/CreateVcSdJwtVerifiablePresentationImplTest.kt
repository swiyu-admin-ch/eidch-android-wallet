package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.foitt.openid4vc.domain.model.KeyPairError
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.usecase.GetHardwareKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.GetSoftwareKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockKeyPairs.VALID_KEY_PAIR_HARDWARE
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.CreateVcSdJwtVerifiablePresentation
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation.CreateVcSdJwtVerifiablePresentationImpl.Companion.HASH_ALGORITHM
import ch.admin.foitt.openid4vc.util.SafeJsonTestInstance
import ch.admin.foitt.openid4vc.util.SafeJsonTestInstance.safeJson
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import ch.admin.foitt.openid4vc.utils.Constants.ANDROID_KEY_STORE
import ch.admin.foitt.openid4vc.utils.createDigest
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.nimbusds.jwt.SignedJWT
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.security.NoSuchAlgorithmException
import java.time.Instant

class CreateVcSdJwtVerifiablePresentationImplTest {
    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var mockGetHardwareKeyPair: GetHardwareKeyPair

    @MockK
    private lateinit var mockGetSoftwareKeyPair: GetSoftwareKeyPair

    @MockK
    private lateinit var mockCredential: VcSdJwtCredential

    @MockK
    private lateinit var mockAuthorizationRequest: AuthorizationRequest

    @MockK
    private lateinit var mockKeyBinding: KeyBinding

    private lateinit var useCase: CreateVcSdJwtVerifiablePresentation

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = CreateVcSdJwtVerifiablePresentationImpl(
            safeJson = safeJson,
            getHardwareKeyPair = mockGetHardwareKeyPair,
            getSoftwareKeyPair = mockGetSoftwareKeyPair,
            defaultDispatcher = testDispatcher
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Creating VcSdJwtVerifiablePresentation without holder binding returns the presentation Jwt without key binding`() =
        runTest(testDispatcher) {
            every { mockCredential.createVerifiableCredential(mockRequestedFields) } returns SD_JWT_WITH_DISCLOSURES

            val result = useCase(
                credential = mockCredential,
                keyBinding = null,
                requestedFields = mockRequestedFields,
                authorizationRequest = mockAuthorizationRequest,
            ).assertOk()

            assertEquals(SD_JWT_WITH_DISCLOSURES, result)
        }

    @Test
    fun `Creating VcSdJwtVerifiablePresentation with holder binding returns the presentation Jwt with proof`() = runTest(testDispatcher) {
        every { mockCredential.cnfJwk } returns SafeJsonTestInstance.json.parseToJsonElement(CREDENTIAL_CNF_JWK)

        val result = useCase(
            credential = mockCredential,
            keyBinding = mockKeyBinding,
            requestedFields = mockRequestedFields,
            authorizationRequest = mockAuthorizationRequest,
        ).assertOk()

        // The proofJwtString is not created the same every time ->
        // check that result is holderBindingSdJwtWithDisclosures plus a jwt that contains the needed values
        val resultSdJwtWithDisclosures = result.substringBeforeLast("~") + "~"
        assertEquals(HOLDER_BINDING_SD_JWT_WITH_DISCLOSURES, resultSdJwtWithDisclosures)

        val resultProofJwt = result.substringAfterLast("~")
        val jwt = SignedJWT.parse(resultProofJwt)
        val jwtHeader = jwt.header
        assertEquals(SIGNING_ALGORITHM.stdName, jwtHeader.algorithm.name)
        assertEquals(CreateVcSdJwtVerifiablePresentationImpl.HEADER_TYPE, jwtHeader.type.type)
        val jwtBody = jwt.jwtClaimsSet
        assertEquals(BASE64_URL_ENCODED_HASH, jwtBody.claims[CreateVcSdJwtVerifiablePresentationImpl.CLAIM_KEY_SD_HASH])
        assertEquals(CLIENT_ID, jwtBody.audience.first())
        assertEquals(NONCE, jwtBody.claims[CreateVcSdJwtVerifiablePresentationImpl.CLAIM_KEY_NONCE])
        assertEquals(ISSUED_AT * 1000, jwtBody.issueTime.time)
    }

    @Test
    fun `Creating VcSdJwtVerifiablePresentation maps errors from getting verifiable credential`() = runTest(testDispatcher) {
        val exception = Exception()
        every { mockCredential.createVerifiableCredential(mockRequestedFields) } throws exception

        useCase(
            credential = mockCredential,
            keyBinding = mockKeyBinding,
            requestedFields = mockRequestedFields,
            authorizationRequest = mockAuthorizationRequest,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `Creating VcSdJwtVerifiablePresentation maps errors from hashing`() = runTest(testDispatcher) {
        val exception = NoSuchAlgorithmException()
        every { any<String>().createDigest(HASH_ALGORITHM) } throws exception

        useCase(
            credential = mockCredential,
            keyBinding = mockKeyBinding,
            requestedFields = mockRequestedFields,
            authorizationRequest = mockAuthorizationRequest,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `Creating VcSdJwtVerifiablePresentation maps errors from getting hardware key pair`() = runTest(testDispatcher) {
        val exception = Exception()
        coEvery { mockGetHardwareKeyPair(any(), any()) } returns Err(KeyPairError.Unexpected(exception))
        every { mockKeyBinding.bindingType } returns KeyBindingType.HARDWARE

        useCase(
            credential = mockCredential,
            keyBinding = mockKeyBinding,
            requestedFields = mockRequestedFields,
            authorizationRequest = mockAuthorizationRequest,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `Creating VcSdJwtVerifiablePresentation maps errors from getting software key pair`() = runTest(testDispatcher) {
        val exception = Exception()
        coEvery { mockGetSoftwareKeyPair(any(), any()) } returns Err(KeyPairError.Unexpected(exception))

        useCase(
            credential = mockCredential,
            keyBinding = mockKeyBinding,
            requestedFields = mockRequestedFields,
            authorizationRequest = mockAuthorizationRequest,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    private fun setupDefaultMocks() {
        every { mockCredential.createVerifiableCredential(mockRequestedFields) } returns HOLDER_BINDING_SD_JWT_WITH_DISCLOSURES

        every { mockKeyBinding.identifier } returns SIGNING_KEY_ID
        every { mockKeyBinding.algorithm } returns SIGNING_ALGORITHM
        every { mockKeyBinding.bindingType } returns KeyBindingType.SOFTWARE
        every { mockKeyBinding.publicKey } returns byteArrayOf()
        every { mockKeyBinding.privateKey } returns byteArrayOf()

        every { mockAuthorizationRequest.clientId } returns CLIENT_ID

        mockkStatic(String::createDigest)
        every { any<String>().createDigest(HASH_ALGORITHM) } returns BASE64_URL_ENCODED_HASH

        mockkStatic(Instant::class)
        every { Instant.now().epochSecond } returns ISSUED_AT

        coEvery { mockGetHardwareKeyPair(SIGNING_KEY_ID, ANDROID_KEY_STORE) } returns Ok(VALID_KEY_PAIR_HARDWARE.keyPair)
        coEvery { mockGetSoftwareKeyPair(any(), any()) } returns Ok(VALID_KEY_PAIR_HARDWARE.keyPair)

        every { mockAuthorizationRequest.nonce } returns NONCE
    }

    private companion object {
        const val ISSUED_AT = 1L
        const val SIGNING_KEY_ID = "signingKeyId"
        val SIGNING_ALGORITHM = SigningAlgorithm.ES256
        val mockRequestedFields = mockk<List<String>>()
        const val NONCE = "nonce"
        const val CLIENT_ID = "clientId"

        const val CREDENTIAL_CNF_JWK = """{"kty":"EC","crv":"P-256","x":"x","y":"y"}"""

/*
-----BEGIN PUBLIC KEY-----
MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAERqVXn+o+6zEOpWEsGw5CsB+wd8zO
jxu0uASGpiGP+wYfcc1unyMxcStbDzUjRuObY8DalaCJ9/J6UrkQkZBtZw==
-----END PUBLIC KEY-----
-----BEGIN PRIVATE KEY-----
MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQglBnO+qn+RecAQ31T
jBklNu+AwiFN5eVHBFbnjecmMryhRANCAARGpVef6j7rMQ6lYSwbDkKwH7B3zM6P
G7S4BIamIY/7Bh9xzW6fIzFxK1sPNSNG45tjwNqVoIn38npSuRCRkG1n
-----END PRIVATE KEY-----
 */

        const val CREDENTIAL_PAYLOAD_WITHOUT_HOLDER_BINDING =
            "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiJ9.eyJfc2QiOlsiLVUzMnpIS2RRNlhZMnVNQ0s0X2c4QkRmMkxJSzVWdkViNW1HRGFmQWFEVSIsIjV5eHlmUVR4YmJacUJWUmdwUjZWUHR3dlZfLUVOZm9oRFNxX1duUzFsSW8iXSwibmJmIjoxNzIyNDk5MjAwLCJfc2RfYWxnIjoic2hhLTI1NiIsImV4cCI6MTc2NzE2ODAwMCwiaWF0IjoxNzI5MjU4NDIwfQ.CZ_YtSqto-e6w1mJ8bUnS3zIeJNtxdPCErnejgTGjUVwb7nAO-JAdqAGd5nO_jejVVnJrZZh8joohGqiLC39wQ"
        const val CREDENTIAL_PAYLOAD_WITH_HOLDER_BINDING =
            "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiJ9.eyJfc2QiOlsiLVUzMnpIS2RRNlhZMnVNQ0s0X2c4QkRmMkxJSzVWdkViNW1HRGFmQWFEVSIsIjV5eHlmUVR4YmJacUJWUmdwUjZWUHR3dlZfLUVOZm9oRFNxX1duUzFsSW8iXSwibmJmIjoxNzIyNDk5MjAwLCJfc2RfYWxnIjoic2hhLTI1NiIsImV4cCI6MTc2NzE2ODAwMCwiaWF0IjoxNzI5MjU4NDIwLCJjbmYiOnsiandrIjp7Imt0eSI6IkVDIiwiY3J2IjoiUC0yNTYiLCJ4IjoiLWdMMHd1dlZfOTFCQ0RfdzZra2ZjSXNyaTFtaEdBa2UwcjdNRkZ5SVM1ayIsInkiOiJOcmIzMDBJV1NJOWFsX2Z2VWtQWUNqU2otUEFxMFc4UGd5TTkzMWFBeWpBIn19fQ.7JtyTNS_tHwtuBYCFz6UNSKJ8Jky2XbHVxdAgkuu6my_wdLLH7KX-wnev3zUX9-BpoPdM_go_63Lg3IUtiqLwQ"
        const val DISCLOSURE1 = "-U32zHKdQ6XY2uMCK4_g8BDf2LIK5VvEb5mGDafAaDU"
        const val DISCLOSURE2 = "5yxyfQTxbbZqBVRgpR6VPtwvV_-ENfohDSq_WnS1lIo"

        const val SD_JWT_WITH_DISCLOSURES = "$CREDENTIAL_PAYLOAD_WITHOUT_HOLDER_BINDING~$DISCLOSURE1~$DISCLOSURE2~"
        const val HOLDER_BINDING_SD_JWT_WITH_DISCLOSURES = "$CREDENTIAL_PAYLOAD_WITH_HOLDER_BINDING~$DISCLOSURE1~$DISCLOSURE2~"

        const val BASE64_URL_ENCODED_HASH = "base64UrlEncodedHash"
    }
}
