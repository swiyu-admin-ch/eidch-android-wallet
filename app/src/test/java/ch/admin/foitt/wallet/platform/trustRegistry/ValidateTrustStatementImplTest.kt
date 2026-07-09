package ch.admin.foitt.wallet.platform.trustRegistry

import ch.admin.foitt.didResolver.domain.DidResolverHelper
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.anycredential.Validity
import ch.admin.foitt.openid4vc.domain.model.jwt.JwtError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwt
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignatureFromDid
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.FetchCredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustDomainFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ValidateTrustStatementImpl
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErr
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class ValidateTrustStatementImplTest {
    @MockK
    private lateinit var mockDidResolverHelper: DidResolverHelper

    @MockK
    private lateinit var mockGetTrustDomainFromDid: GetTrustDomainFromDid

    @MockK
    private lateinit var mockEnvironmentSetup: EnvironmentSetupRepository

    @MockK
    private lateinit var mockVerifyJwtSignatureFromDid: VerifyJwtSignatureFromDid

    @MockK
    private lateinit var mockFetchCredentialStatus: FetchCredentialStatus

    @MockK
    private lateinit var trustStatement: VcSdJwt

    private val testSafeJson = SafeJsonTestInstance.safeJson

    private lateinit var useCase: ValidateTrustStatement

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = ValidateTrustStatementImpl(
            didResolverHelper = mockDidResolverHelper,
            getTrustDomainFromDid = mockGetTrustDomainFromDid,
            environmentSetupRepo = mockEnvironmentSetup,
            verifyJwtSignatureFromDid = mockVerifyJwtSignatureFromDid,
            safeJson = testSafeJson,
            fetchCredentialStatus = mockFetchCredentialStatus
        )

        every { trustStatement.kid } returns KEY_ID
        every { trustStatement.type } returns ValidateTrustStatementImpl.VCSDJWT_TYPE_VALUE
        every { trustStatement.algorithm } returns SigningAlgorithm.ES256.stdName
        every { trustStatement.issuedAt } returns Instant.ofEpochSecond(0)
        every { trustStatement.subject } returns ACTOR_DID
        every { trustStatement.jwtValidity } returns Validity.Valid
        every { trustStatement.status } returns buildJsonObject {
            put(
                "status_list",
                buildJsonObject {
                    put("idx", JsonPrimitive(0))
                    put("uri", JsonPrimitive("uri"))
                }
            )
        }

        every {
            mockDidResolverHelper.getDidStringFromAbsoluteKeyId(KEY_ID)
        } returns Ok(ACTOR_DID)

        coEvery {
            mockGetTrustDomainFromDid(ACTOR_DID)
        } returns Ok(TRUST_DOMAIN)

        coEvery {
            mockEnvironmentSetup.trustV1TrustRegistryTrustedDids
        } returns trustedDids

        coEvery { mockVerifyJwtSignatureFromDid(kid = any(), jwt = any()) } returns Ok(Unit)
        coEvery {
            mockFetchCredentialStatus(credentialIssuer = any(), properties = any())
        } returns Ok(CredentialStatus.VALID)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A valid trust statement passes validation`(): Unit = runTest {
        useCase(trustStatement, ACTOR_DID).assertOk()

        coVerify(exactly = 1) {
            mockEnvironmentSetup.trustV1TrustRegistryTrustedDids
            mockVerifyJwtSignatureFromDid(kid = any(), jwt = any())
        }
    }

    @Test
    fun `A did resolver error is mapped`() = runTest {
        val exception = IllegalStateException("did error")
        every {
            mockDidResolverHelper.getDidStringFromAbsoluteKeyId(KEY_ID)
        } returns Err(exception)

        useCase(trustStatement, ACTOR_DID).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A trust statement where getting the trust domain fails is considered not trusted`(): Unit = runTest {
        val exception = IllegalStateException("trust domain error")
        coEvery {
            mockGetTrustDomainFromDid(ACTOR_DID)
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(trustStatement, ACTOR_DID).assertErr()

        coVerify(exactly = 1) {
            mockGetTrustDomainFromDid(ACTOR_DID)
        }
        coVerify(exactly = 0) {
            mockEnvironmentSetup.trustV1TrustRegistryTrustedDids
            mockVerifyJwtSignatureFromDid(kid = any(), jwt = any())
        }
    }

    @Test
    fun `A trust statement not whitelisted fails validation`(): Unit = runTest {
        every { mockDidResolverHelper.getDidStringFromAbsoluteKeyId(KEY_ID) } returns Ok("otherDid")

        useCase(trustStatement, ACTOR_DID).assertErrorType(TrustRegistryError.Unexpected::class)

        coVerify(exactly = 1) {
            mockGetTrustDomainFromDid(ACTOR_DID)
            mockEnvironmentSetup.trustV1TrustRegistryTrustedDids
        }
        coVerify(exactly = 0) {
            mockVerifyJwtSignatureFromDid(kid = any(), jwt = any())
        }
    }

    @Test
    fun `A trust statement declaring the wrong type fails validation`(): Unit = runTest {
        every { trustStatement.type } returns "otherType"
        useCase(trustStatement, ACTOR_DID).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A trust statement declaring the wrong algorithm fails validation`(): Unit = runTest {
        every { trustStatement.algorithm } returns "otherAlgorithm"
        useCase(trustStatement, ACTOR_DID).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A trust statement with an invalid signature fails validation`(): Unit = runTest {
        coEvery {
            mockVerifyJwtSignatureFromDid(kid = KEY_ID, jwt = trustStatement)
        } returns Err(JwtError.InvalidJwt)

        useCase(trustStatement, ACTOR_DID).assertErrorType(TrustRegistryError.Unexpected::class)

        coVerify(exactly = 1) {
            mockEnvironmentSetup.trustV1TrustRegistryTrustedDids
            mockVerifyJwtSignatureFromDid(kid = KEY_ID, jwt = trustStatement)
        }
    }

    @Test
    fun `A trust statement missing the iat claim fails validation`(): Unit = runTest {
        every { trustStatement.issuedAt } returns null
        useCase(trustStatement, ACTOR_DID).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A trust statement missing the sub claim fails validation`(): Unit = runTest {
        every { trustStatement.subject } returns null
        useCase(trustStatement, ACTOR_DID).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A trust statement where the input actor did does not match the subject fails validation`() = runTest {
        useCase(trustStatement, "otherActor").assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A trust statement where the subject does not match the input actor did fails validation`() = runTest {
        every { trustStatement.subject } returns "otherSubject"
        useCase(trustStatement, ACTOR_DID).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A trust statement with an invalid validity fails validation`() = runTest {
        every { trustStatement.jwtValidity } returns Validity.Expired(Instant.now())
        useCase(trustStatement, ACTOR_DID).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A trust statement missing status property fails validation`(): Unit = runTest {
        every { trustStatement.status } returns null
        useCase(trustStatement, ACTOR_DID).assertErrorType(TrustRegistryError.Unexpected::class)

        coVerify(exactly = 0) {
            mockFetchCredentialStatus(any(), any())
        }
    }

    @Test
    fun `A failed status list call fails validation`(): Unit = runTest {
        coEvery { mockFetchCredentialStatus(any(), any()) } returns Err(CredentialStatusError.NetworkError)

        useCase(trustStatement, ACTOR_DID).assertErrorType(TrustRegistryError.Unexpected::class)

        coVerify(exactly = 1) {
            mockFetchCredentialStatus(any(), any())
        }
    }

    @Test
    fun `A trust statement with any other status than valid fails validation`(): Unit = runTest {
        coEvery { mockFetchCredentialStatus(any(), any()) } returns Ok(CredentialStatus.SUSPENDED)

        useCase(trustStatement, ACTOR_DID).assertErrorType(TrustRegistryError.Unexpected::class)

        coVerify(exactly = 1) {
            mockFetchCredentialStatus(any(), any())
        }
    }

    companion object {
        private const val ACTOR_DID = "did:tdw:abc"
        private const val KEY_ID = "$ACTOR_DID#key-01"
        private const val TRUST_DOMAIN = "example.org"
        private val trustedDids = mapOf(
            TRUST_DOMAIN to listOf(ACTOR_DID)
        )
    }
}
