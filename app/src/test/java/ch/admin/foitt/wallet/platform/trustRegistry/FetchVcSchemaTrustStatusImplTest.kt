package ch.admin.foitt.wallet.platform.trustRegistry

import ch.admin.foitt.didResolver.domain.DidResolverHelper
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwt
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementActor
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.repository.TrustStatementRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.FetchVcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustDomainFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustUrlFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.FetchVcSchemaTrustStatusImpl
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class FetchVcSchemaTrustStatusImplTest {

    @MockK
    private lateinit var mockGetTrustUrlFromDid: GetTrustUrlFromDid

    @MockK
    private lateinit var mockTrustStatementRepository: TrustStatementRepository

    @MockK
    private lateinit var mockGetTrustDomainFromDid: GetTrustDomainFromDid

    @MockK
    private lateinit var mockDidResolverHelper: DidResolverHelper

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    @MockK
    private lateinit var mockValidateTrustStatement: ValidateTrustStatement

    private val safeJson = SafeJsonTestInstance.safeJson

    private lateinit var useCase: FetchVcSchemaTrustStatus

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = FetchVcSchemaTrustStatusImpl(
            getTrustUrlFromDid = mockGetTrustUrlFromDid,
            trustStatementRepository = mockTrustStatementRepository,
            getTrustDomainFromDid = mockGetTrustDomainFromDid,
            didResolverHelper = mockDidResolverHelper,
            environmentSetupRepo = mockEnvironmentSetupRepository,
            validateTrustStatement = mockValidateTrustStatement,
            safeJson = safeJson
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `vc schema trust status for issuance is fetched correctly`() = runTest {
        val result = useCase(
            trustStatementActor = TrustStatementActor.ISSUER,
            actorDid = actorDid,
            vcSchemaId = vcSchemaId,
        ).assertOk()

        assertEquals(VcSchemaTrustStatus.TRUSTED, result)
    }

    @Test
    fun `vc schema trust status for verification is fetched correctly`() = runTest {
        coEvery {
            mockTrustStatementRepository.fetchTrustStatements(trustUrl)
        } returns Ok(listOf(validVerificationTrustStatement))

        coEvery {
            mockValidateTrustStatement(any(), any())
        } returns Ok(VcSdJwt(validVerificationTrustStatement))

        val result = useCase(
            trustStatementActor = TrustStatementActor.VERIFIER,
            actorDid = actorDid,
            vcSchemaId = vcSchemaId,
        ).assertOk()

        assertEquals(VcSchemaTrustStatus.TRUSTED, result)
    }

    @Test
    fun `fetching vc schema trust maps errors from getting the trust url`() = runTest {
        coEvery {
            mockGetTrustUrlFromDid(any(), actorDid, vcSchemaId)
        } returns Err(TrustRegistryError.Unexpected(IllegalStateException("get trust url error")))

        useCase(
            trustStatementActor = TrustStatementActor.ISSUER,
            actorDid = actorDid,
            vcSchemaId = vcSchemaId,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `fetching vc schema trust maps errors from the trust repo`() = runTest {
        coEvery {
            mockTrustStatementRepository.fetchTrustStatements(trustUrl)
        } returns Err(TrustRegistryError.Unexpected(null))

        useCase(
            trustStatementActor = TrustStatementActor.ISSUER,
            actorDid = actorDid,
            vcSchemaId = vcSchemaId,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `fetching vc schema trust where a trust statement is a invalid VcSdJwt returns an error`() = runTest {
        coEvery {
            mockTrustStatementRepository.fetchTrustStatements(trustUrl)
        } returns Ok(listOf(invalidVcSdJwtTrustStatement))

        useCase(
            trustStatementActor = TrustStatementActor.ISSUER,
            actorDid = actorDid,
            vcSchemaId = vcSchemaId,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `fetching vc schema trust where no trust statements with the correct vct exist returns unprotected`() = runTest {
        coEvery {
            mockTrustStatementRepository.fetchTrustStatements(trustUrl)
        } returns Ok(listOf(trustStatementOtherVct))

        val result = useCase(
            trustStatementActor = TrustStatementActor.VERIFIER,
            actorDid = actorDid,
            vcSchemaId = vcSchemaId,
        ).assertOk()

        assertEquals(VcSchemaTrustStatus.UNPROTECTED, result)
    }

    @Test
    fun `fetching vc schema trust where no getting the trust domain fails returns unprotected`() = runTest {
        val exception = IllegalStateException("trust domain error")
        coEvery {
            mockGetTrustDomainFromDid(any())
        } returns Err(TrustRegistryError.Unexpected(exception))

        val result = useCase(
            trustStatementActor = TrustStatementActor.VERIFIER,
            actorDid = actorDid,
            vcSchemaId = vcSchemaId,
        ).assertOk()

        assertEquals(VcSchemaTrustStatus.UNPROTECTED, result)

        coVerify(exactly = 0) {
            mockEnvironmentSetupRepository.trustV1TrustRegistryTrustedDids
        }
    }

    @Test
    fun `fetching vc schema trust where no getting the did string fails returns unprotected`() = runTest {
        val exception = IllegalStateException("trust domain error")
        coEvery {
            mockDidResolverHelper.getDidStringFromAbsoluteKeyId(any())
        } returns Err(exception)

        val result = useCase(
            trustStatementActor = TrustStatementActor.VERIFIER,
            actorDid = actorDid,
            vcSchemaId = vcSchemaId,
        ).assertOk()

        assertEquals(VcSchemaTrustStatus.UNPROTECTED, result)

        coVerify(exactly = 0) {
            mockEnvironmentSetupRepository.trustV1TrustRegistryTrustedDids
        }
    }

    @Test
    fun `fetching vc schema trust where no trust statements with trusted did exist returns unprotected`() = runTest {
        coEvery {
            mockTrustStatementRepository.fetchTrustStatements(trustUrl)
        } returns Ok(listOf(trustStatementOtherKid))

        val result = useCase(
            trustStatementActor = TrustStatementActor.VERIFIER,
            actorDid = actorDid,
            vcSchemaId = vcSchemaId,
        ).assertOk()

        assertEquals(VcSchemaTrustStatus.UNPROTECTED, result)
    }

    @Test
    fun `fetching vc schema trust where no valid trust statements exist returns not trusted`() = runTest {
        coEvery {
            mockValidateTrustStatement(any(), any())
        } returns Err(TrustRegistryError.Unexpected(null))

        val result = useCase(
            trustStatementActor = TrustStatementActor.ISSUER,
            actorDid = actorDid,
            vcSchemaId = vcSchemaId,
        ).assertOk()

        assertEquals(VcSchemaTrustStatus.NOT_TRUSTED, result)
    }

    @Test
    fun `fetching vc schema trust where multiple valid trust statements exist returns not_trusted`() = runTest {
        coEvery {
            mockTrustStatementRepository.fetchTrustStatements(trustUrl)
        } returns Ok(listOf(validIssuanceTrustStatement, validIssuanceTrustStatement2))

        coEvery {
            mockValidateTrustStatement(any(), any())
        } returnsMany listOf(
            Ok(VcSdJwt(validIssuanceTrustStatement)),
            Ok(VcSdJwt(validIssuanceTrustStatement2))
        )

        val result = useCase(
            trustStatementActor = TrustStatementActor.ISSUER,
            actorDid = actorDid,
            vcSchemaId = vcSchemaId,
        ).assertOk()

        assertEquals(VcSchemaTrustStatus.NOT_TRUSTED, result)
    }

    @Test
    fun `fetching vc schema trust where trust statement is valid but with incorrect vcSchemaId returns not trusted`() = runTest {
        coEvery {
            mockTrustStatementRepository.fetchTrustStatements(trustUrl)
        } returns Ok(listOf(trustStatementOtherVcSchemaId))

        coEvery {
            mockValidateTrustStatement(any(), any())
        } returns Ok(VcSdJwt(trustStatementOtherVcSchemaId))

        val result = useCase(
            trustStatementActor = TrustStatementActor.ISSUER,
            actorDid = actorDid,
            vcSchemaId = vcSchemaId,
        ).assertOk()

        assertEquals(VcSchemaTrustStatus.NOT_TRUSTED, result)
    }

    private fun setupDefaultMocks() {
        coEvery { mockGetTrustUrlFromDid(any(), actorDid, vcSchemaId) } returns Ok(trustUrl)

        coEvery {
            mockTrustStatementRepository.fetchTrustStatements(trustUrl)
        } returns Ok(listOf(validIssuanceTrustStatement))

        coEvery { mockGetTrustDomainFromDid(actorDid) } returns Ok(trustDomain)

        every { mockDidResolverHelper.getDidStringFromAbsoluteKeyId(keyId1) } returns Ok(issuer1)
        every { mockDidResolverHelper.getDidStringFromAbsoluteKeyId(keyId2) } returns Ok(issuer2)
        every { mockDidResolverHelper.getDidStringFromAbsoluteKeyId(otherKeyId) } returns Ok(otherIssuer)

        coEvery { mockEnvironmentSetupRepository.trustV1TrustRegistryTrustedDids } returns trustedIssuers

        coEvery {
            mockValidateTrustStatement(any(), any())
        } returns Ok(VcSdJwt(validIssuanceTrustStatement))
    }

    private val actorDid = "actorDid"
    private val vcSchemaId = "vcSchemaId"

    private val trustDomain = "example.org"

    private val trustUrl = URL("https://$trustDomain/trust")

    private val issuer1 = "did:tdw:abc"
    private val issuer2 = "did:example:issuer"
    private val otherIssuer = "otherissuer"
    private val keyId1 = "$issuer1#key-01"
    private val keyId2 = "$issuer2#key-01"
    private val otherKeyId = "$otherIssuer#key-01"

    private val trustedIssuers = mapOf(
        trustDomain to listOf(issuer1, issuer2)
    )

    private val validIssuanceTrustStatement =
        "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleS0wMSJ9.eyJ2Y3QiOiJUcnVzdFN0YXRlbWVudElzc3VhbmNlVjEiLCJpYXQiOjE3NDI0NTMyMTEsInN0YXR1cyI6eyJzdGF0dXNfbGlzdCI6eyJ1cmkiOiJzdGF0dXNfbGlzdF91cmkiLCJpZHgiOjMwfX0sIm5iZiI6MTc0MjQ1MzIxMCwiZXhwIjoyMjA5MDE0MDAwLCJfc2QiOlsiNFoteUd2Z0JmcXh5Y3RNTlotb0duSVk0R2h5d3JXeWlocmNkQXBsNUNLQSIsInZjUlhhU2hNU2ZNSWpyXzdwTnRfN1VKcW4zdUNpNGY4NnA0R0ppRm1hNmciXSwiX3NkX2FsZyI6IlNIQS0yNTYifQ.IzETA9aMUebUL4x2CtFPCLrTE3G97gFUSP3ck3VbgsdZhoxsNgk9N-5W4S4LLp69R0-n8Y1uKc-sKOOCA3Wgyg~WyJhYTExODJkN2ZhNjg0MTAyIiwic3ViIiwic3ViamVjdCJd~WyIyYjcwNDcyNTFmY2FiMzcyIiwiY2FuSXNzdWUiLCJ2Y1NjaGVtYUlkIl0~"

    private val validIssuanceTrustStatement2 =
        "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDpleGFtcGxlOmlzc3VlciNrZXktMDEifQ.eyJfc2QiOlsiUWJaSmJ4TjBfOHhPV2p3Y3JROVlYSzZPYmc5VXQ2cUZnOWpGS1JSTVdTVSIsIkl0dGlZTUliLTlYV2tSTDRsLWtMQm10bU04bmlkWHduOWR2SHMwbXRMMUEiXSwidmN0IjoiVHJ1c3RTdGF0ZW1lbnRJc3N1YW5jZVYxIiwiaWF0IjoxNzQyNDUzMjExLCJzdGF0dXMiOnsic3RhdHVzX2xpc3QiOnsidXJpIjoic3RhdHVzX2xpc3RfdXJpIiwiaWR4IjozMH19LCJuYmYiOjE3NDI0NTMyMTAsImV4cCI6MjIwOTAxNDAwMCwiX3NkX2FsZyI6IlNIQS0yNTYifQ.MoWado6P-zU8akElAdrnqtZlxdRAPXM1vXjwPk4FepcuUZFMlxM79cfTPXxguefj24iXgOC49IKMKdt6oooSJw~WyIxZGRkYWY2YmQ2Y2ExYTc5IiwiY2FuSXNzdWUiLCJ2Y1NjaGVtYUlkIl0~WyIxZGRkYWY2YmRkY2ExYTc5Iiwic3ViIiwic3ViamVjdCJd~"

    private val validVerificationTrustStatement =
        "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleS0wMSJ9.eyJfc2QiOlsibEVUTUlfZ25tOG8tb1ZaRVNYUzB3UE9OSmpLR1Vpamw1enNXOUM4YjZ3byIsIkl0dGlZTUliLTlYV2tSTDRsLWtMQm10bU04bmlkWHduOWR2SHMwbXRMMUEiXSwidmN0IjoiVHJ1c3RTdGF0ZW1lbnRWZXJpZmljYXRpb25WMSIsImlhdCI6MTc0MjQ1MzIxMSwic3RhdHVzIjp7InN0YXR1c19saXN0Ijp7InVyaSI6InN0YXR1c19saXN0X3VyaSIsImlkeCI6MzB9fSwibmJmIjoxNzQyNDUzMjEwLCJleHAiOjIyMDkwMTQwMDAsIl9zZF9hbGciOiJTSEEtMjU2In0.GxVVIU4bQ9FS-O2eYV2BJnSleOTBvj3ossuaAUUb4Z2GHROh_of9kB5Gvry4jA2jWJYUfYZ_VxVBH-rf1SgvwQ~WyIxZGRkYWY2YmQ2Y2ExYTc5IiwiY2FuVmVyaWZ5IiwidmNTY2hlbWFJZCJd~WyIxZGRkYWY2YmRkY2ExYTc5Iiwic3ViIiwic3ViamVjdCJd~"

    private val trustStatementOtherVct =
        "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleS0wMSJ9.eyJfc2QiOlsiUWJaSmJ4TjBfOHhPV2p3Y3JROVlYSzZPYmc5VXQ2cUZnOWpGS1JSTVdTVSIsIkl0dGlZTUliLTlYV2tSTDRsLWtMQm10bU04bmlkWHduOWR2SHMwbXRMMUEiXSwidmN0Ijoib3RoZXJWY3QiLCJpYXQiOjE3NDI0NTMyMTEsInN0YXR1cyI6eyJzdGF0dXNfbGlzdCI6eyJ1cmkiOiJzdGF0dXNfbGlzdF91cmkiLCJpZHgiOjMwfX0sIm5iZiI6MTc0MjQ1MzIxMCwiZXhwIjoyMjA5MDE0MDAwLCJfc2RfYWxnIjoiU0hBLTI1NiJ9.RoBVv2YAZKyhFdXhEVVhi5NzS00Nj4VYpijbDP56yINo9ZDup_ZNAnTLV1k715j6d_MdB4bM3eLEy-B_G22g1Q~"

    private val trustStatementOtherKid =
        "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDpleGFtcGxlOm90aGVyaXNzdWVyI2tleS0wMSJ9.eyJfc2QiOlsiUWJaSmJ4TjBfOHhPV2p3Y3JROVlYSzZPYmc5VXQ2cUZnOWpGS1JSTVdTVSIsIkl0dGlZTUliLTlYV2tSTDRsLWtMQm10bU04bmlkWHduOWR2SHMwbXRMMUEiXSwidmN0IjoiVHJ1c3RTdGF0ZW1lbnRJc3N1YW5jZVYxIiwiaWF0IjoxNzQyNDUzMjExLCJzdGF0dXMiOnsic3RhdHVzX2xpc3QiOnsidXJpIjoic3RhdHVzX2xpc3RfdXJpIiwiaWR4IjozMH19LCJuYmYiOjE3NDI0NTMyMTAsImV4cCI6MjIwOTAxNDAwMCwiX3NkX2FsZyI6IlNIQS0yNTYifQ.qAoJ85dBBseUe44nvkfoUrYBmv6BEM8wLsxwU7uREyCsL2s7-yNwlm-_dIN_rfQIzE9YpIFZWEOMnLDDu-b8hg~"

    private val invalidVcSdJwtTrustStatement =
        "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleS0wMSJ9.eyJ2Y3QiOiJUcnVzdFN0YXRlbWVudElzc3VhbmNlVjEiLCJpYXQiOjE3NDI0NTMyMTEsInN0YXR1cyI6eyJzdGF0dXNfbGlzdCI6eyJ1cmkiOiJzdGF0dXNfbGlzdF91cmkiLCJpZHgiOjMwfX0sIm5iZiI6MTc0MjQ1MzIxMCwiZXhwIjoyMjA5MDE0MDAwLCJjYW5Jc3N1ZSI6InZjU2NoZW1hSWQiLCJfc2QiOlsiYnJfNEJYNGpodW1yREltTHQyNXlqNzVTTThvODZocFV6dzhzQ2ZFb1J6USJdLCJfc2RfYWxnIjoiU0hBLTI1NiJ9.DH7dMBbXyYfxfvBXwYC7t0ByVHWJhhh6mSjgjw_RlhdRRhcz2tQM2iBul7l1M6R3DhTYaBEoYrWwGRiJgtSwpQ~"

    private val trustStatementOtherVcSchemaId =
        "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleS0wMSJ9.eyJfc2QiOlsiR3c0MmNBT29uUDludTYySnROUjhrZFNhRFRiekhvUTdieUduVEZ4QWladyIsIkl0dGlZTUliLTlYV2tSTDRsLWtMQm10bU04bmlkWHduOWR2SHMwbXRMMUEiXSwidmN0IjoiVHJ1c3RTdGF0ZW1lbnRJc3N1YW5jZVYxIiwiaWF0IjoxNzQyNDUzMjExLCJzdGF0dXMiOnsic3RhdHVzX2xpc3QiOnsidXJpIjoic3RhdHVzX2xpc3RfdXJpIiwiaWR4IjozMH19LCJuYmYiOjE3NDI0NTMyMTAsImV4cCI6MjIwOTAxNDAwMCwiX3NkX2FsZyI6IlNIQS0yNTYifQ.ULraS1FDL4-wMyj8dd_71Q3qArBGbWVZ7U6BTrBnF2gb9eTRehvIklTLPhJeX8d6RViCQE4t9DoHS5Btt9K43g~"
}
