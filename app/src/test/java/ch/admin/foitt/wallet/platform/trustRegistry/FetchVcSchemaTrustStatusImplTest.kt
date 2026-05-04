package ch.admin.foitt.wallet.platform.trustRegistry

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
        coEvery {
            mockTrustStatementRepository.fetchTrustStatements(trustUrl)
        } returns Ok(listOf(trustStatementOtherIssuer))

        val exception = IllegalStateException("trust domain error")
        coEvery {
            mockGetTrustDomainFromDid(actorDid)
        } returns Err(TrustRegistryError.Unexpected(exception))

        val result = useCase(
            trustStatementActor = TrustStatementActor.VERIFIER,
            actorDid = actorDid,
            vcSchemaId = vcSchemaId,
        ).assertOk()

        assertEquals(VcSchemaTrustStatus.UNPROTECTED, result)

        coVerify(exactly = 0) {
            mockEnvironmentSetupRepository.trustRegistryTrustedDids
        }
    }

    @Test
    fun `fetching vc schema trust where no trust statements with trusted did exist returns unprotected`() = runTest {
        coEvery {
            mockTrustStatementRepository.fetchTrustStatements(trustUrl)
        } returns Ok(listOf(trustStatementOtherIssuer))

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

        coEvery { mockEnvironmentSetupRepository.trustRegistryTrustedDids } returns trustedIssuers

        coEvery {
            mockValidateTrustStatement(any(), any())
        } returns Ok(VcSdJwt(validIssuanceTrustStatement))
    }

    private val actorDid = "actorDid"
    private val vcSchemaId = "vcSchemaId"

    private val trustDomain = "example.org"

    private val trustUrl = URL("https://$trustDomain/trust")

    private val trustedIssuers = mapOf(
        trustDomain to listOf("issuer", "issuer2")
    )

    private val validIssuanceTrustStatement =
        "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJ2Y3QiOiJUcnVzdFN0YXRlbWVudElzc3VhbmNlVjEiLCJpc3MiOiJpc3N1ZXIiLCJpYXQiOjE3NDI0NTMyMTEsInN0YXR1cyI6eyJzdGF0dXNfbGlzdCI6eyJ1cmkiOiJzdGF0dXNfbGlzdF91cmkiLCJpZHgiOjMwfX0sIm5iZiI6MTc0MjQ1MzIxMCwiZXhwIjoyMjA5MDE0MDAwLCJfc2QiOlsiNFoteUd2Z0JmcXh5Y3RNTlotb0duSVk0R2h5d3JXeWlocmNkQXBsNUNLQSIsInZjUlhhU2hNU2ZNSWpyXzdwTnRfN1VKcW4zdUNpNGY4NnA0R0ppRm1hNmciXSwiX3NkX2FsZyI6IlNIQS0yNTYifQ.KW0wRdfa3NivxBLBYokln0cTDLc8gwOritzI8TLNjPijTzvPLTPUnjqimGpEab2RtM8wyVOKNmRaXjhgy_GSIA~WyJhYTExODJkN2ZhNjg0MTAyIiwic3ViIiwic3ViamVjdCJd~WyIyYjcwNDcyNTFmY2FiMzcyIiwiY2FuSXNzdWUiLCJ2Y1NjaGVtYUlkIl0~"

    private val validIssuanceTrustStatement2 =
        "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDpleGFtcGxlOmlzc3VlciNrZXktMSJ9.eyJfc2QiOlsiUWJaSmJ4TjBfOHhPV2p3Y3JROVlYSzZPYmc5VXQ2cUZnOWpGS1JSTVdTVSIsIkl0dGlZTUliLTlYV2tSTDRsLWtMQm10bU04bmlkWHduOWR2SHMwbXRMMUEiXSwidmN0IjoiVHJ1c3RTdGF0ZW1lbnRJc3N1YW5jZVYxIiwiaXNzIjoiaXNzdWVyIiwiaWF0IjoxNzQyNDUzMjExLCJzdGF0dXMiOnsic3RhdHVzX2xpc3QiOnsidXJpIjoic3RhdHVzX2xpc3RfdXJpIiwiaWR4IjozMH19LCJuYmYiOjE3NDI0NTMyMTAsImV4cCI6MjIwOTAxNDAwMCwiX3NkX2FsZyI6IlNIQS0yNTYifQ._9x7-o6Qiydpoo_EZr-ycbxxmKbBGGz-pRJtq_m7eTAakgdGH-Nqdj3J2jGyuMAB9ZpeW6XHSUF1OJmxbarUww~WyIxZGRkYWY2YmQ2Y2ExYTc5IiwiY2FuSXNzdWUiLCJ2Y1NjaGVtYUlkIl0~WyIxZGRkYWY2YmRkY2ExYTc5Iiwic3ViIiwic3ViamVjdCJd~"

    private val validVerificationTrustStatement =
        "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJfc2QiOlsibEVUTUlfZ25tOG8tb1ZaRVNYUzB3UE9OSmpLR1Vpamw1enNXOUM4YjZ3byIsIkl0dGlZTUliLTlYV2tSTDRsLWtMQm10bU04bmlkWHduOWR2SHMwbXRMMUEiXSwidmN0IjoiVHJ1c3RTdGF0ZW1lbnRWZXJpZmljYXRpb25WMSIsImlzcyI6Imlzc3VlciIsImlhdCI6MTc0MjQ1MzIxMSwic3RhdHVzIjp7InN0YXR1c19saXN0Ijp7InVyaSI6InN0YXR1c19saXN0X3VyaSIsImlkeCI6MzB9fSwibmJmIjoxNzQyNDUzMjEwLCJleHAiOjIyMDkwMTQwMDAsIl9zZF9hbGciOiJTSEEtMjU2In0.e93J5cSRy3v2UsTIHc3liIeFl6ZO3wNjkomFwi3pkak3MQAQ5MvpV4azAEiIhTnbmrh55yl2i6Tzz0yMKqCF6w~WyIxZGRkYWY2YmQ2Y2ExYTc5IiwiY2FuVmVyaWZ5IiwidmNTY2hlbWFJZCJd~WyIxZGRkYWY2YmRkY2ExYTc5Iiwic3ViIiwic3ViamVjdCJd~"

    private val trustStatementOtherVct =
        "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJfc2QiOlsiUWJaSmJ4TjBfOHhPV2p3Y3JROVlYSzZPYmc5VXQ2cUZnOWpGS1JSTVdTVSIsIkl0dGlZTUliLTlYV2tSTDRsLWtMQm10bU04bmlkWHduOWR2SHMwbXRMMUEiXSwidmN0Ijoib3RoZXJWY3QiLCJpc3MiOiJpc3N1ZXIiLCJpYXQiOjE3NDI0NTMyMTEsInN0YXR1cyI6eyJzdGF0dXNfbGlzdCI6eyJ1cmkiOiJzdGF0dXNfbGlzdF91cmkiLCJpZHgiOjMwfX0sIm5iZiI6MTc0MjQ1MzIxMCwiZXhwIjoyMjA5MDE0MDAwLCJfc2RfYWxnIjoiU0hBLTI1NiJ9.zT13FOlK_lsq1HmXjt3pJ-wjE3pLK_rP99U5Dd3RyTPyad7Z6EgqhGNTT_qfoZhaOEr-Y_mgKT-pZ-QXG4McxQ~WyIxZGRkYWY2YmQ2Y2ExYTc5IiwiY2FuSXNzdWUiLCJ2Y1NjaGVtYUlkIl0~WyIxZGRkYWY2YmRkY2ExYTc5Iiwic3ViIiwic3ViamVjdCJd~"

    private val trustStatementOtherIssuer =
        "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDpleGFtcGxlOmlzc3VlciNrZXktMSJ9.eyJfc2QiOlsiUWJaSmJ4TjBfOHhPV2p3Y3JROVlYSzZPYmc5VXQ2cUZnOWpGS1JSTVdTVSIsIkl0dGlZTUliLTlYV2tSTDRsLWtMQm10bU04bmlkWHduOWR2SHMwbXRMMUEiXSwidmN0IjoiVHJ1c3RTdGF0ZW1lbnRJc3N1YW5jZVYxIiwiaXNzIjoib3RoZXIgaXNzdWVyIiwiaWF0IjoxNzQyNDUzMjExLCJzdGF0dXMiOnsic3RhdHVzX2xpc3QiOnsidXJpIjoic3RhdHVzX2xpc3RfdXJpIiwiaWR4IjozMH19LCJuYmYiOjE3NDI0NTMyMTAsImV4cCI6MjIwOTAxNDAwMCwiX3NkX2FsZyI6IlNIQS0yNTYifQ.f0ansmO0XwxV5oL1bGInjtZ2Wvyt4XWdvJcX6KNS0y4c9A9zHd04MKbzRLpqDizDx6AHjO90CiscB4dxTODvgQ~WyIxZGRkYWY2YmQ2Y2ExYTc5IiwiY2FuSXNzdWUiLCJ2Y1NjaGVtYUlkIl0~WyIxZGRkYWY2YmRkY2ExYTc5Iiwic3ViIiwic3ViamVjdCJd~"

    private val invalidVcSdJwtTrustStatement =
        "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJ2Y3QiOiJUcnVzdFN0YXRlbWVudElzc3VhbmNlVjEiLCJpYXQiOjE3NDI0NTMyMTEsInN0YXR1cyI6eyJzdGF0dXNfbGlzdCI6eyJ1cmkiOiJzdGF0dXNfbGlzdF91cmkiLCJpZHgiOjMwfX0sIm5iZiI6MTc0MjQ1MzIxMCwiZXhwIjoyMjA5MDE0MDAwLCJjYW5Jc3N1ZSI6InZjU2NoZW1hSWQiLCJfc2QiOlsiYnJfNEJYNGpodW1yREltTHQyNXlqNzVTTThvODZocFV6dzhzQ2ZFb1J6USJdLCJfc2RfYWxnIjoiU0hBLTI1NiJ9.9elIIHP89Ig9MwJxwxq75elKT1vAatJw2yRmCT53FPXLcokmGtwtfSPqSg-Rk9jgRszxJQiaLAucPOCdBU5Rmg~"

    private val trustStatementOtherVcSchemaId =
        "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJfc2QiOlsiR3c0MmNBT29uUDludTYySnROUjhrZFNhRFRiekhvUTdieUduVEZ4QWladyIsIkl0dGlZTUliLTlYV2tSTDRsLWtMQm10bU04bmlkWHduOWR2SHMwbXRMMUEiXSwidmN0IjoiVHJ1c3RTdGF0ZW1lbnRJc3N1YW5jZVYxIiwiaXNzIjoiaXNzdWVyIiwiaWF0IjoxNzQyNDUzMjExLCJzdGF0dXMiOnsic3RhdHVzX2xpc3QiOnsidXJpIjoic3RhdHVzX2xpc3RfdXJpIiwiaWR4IjozMH19LCJuYmYiOjE3NDI0NTMyMTAsImV4cCI6MjIwOTAxNDAwMCwiX3NkX2FsZyI6IlNIQS0yNTYifQ.g9z1CsJ49MOnb9R17EsCe3RvOKhKdQ-R-J7Akf4ksBOtuYHGFTHwNkwcvUYYj1zifhzzFwZjPreUq4gFPL2k2Q~WyIxZGRkYWY2YmQ2Y2ExYTc5IiwiY2FuSXNzdWUiLCJvdGhlclZjU2NoZW1hSWQiXQ~WyIxZGRkYWY2YmRkY2ExYTc5Iiwic3ViIiwic3ViamVjdCJd~"
}
