package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.actorEnvironment.domain.model.ActorEnvironment
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.usecase.GetActorEnvironment
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV1TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustCheckResult
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementActor
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.FetchVcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityV1TrustStatement
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class FetchTrustForIssuanceImplTest {
    @MockK
    private lateinit var mockGetActorEnvironment: GetActorEnvironment

    @MockK
    private lateinit var mockProcessIdentityV1TrustStatement: ProcessIdentityV1TrustStatement

    @MockK
    private lateinit var mockFetchVcSchemaTrustStatus: FetchVcSchemaTrustStatus

    @MockK
    private lateinit var mockIdentityTrustStatement: IdentityV1TrustStatement

    private lateinit var useCase: FetchTrustForIssuanceImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = FetchTrustForIssuanceImpl(
            getActorEnvironment = mockGetActorEnvironment,
            processIdentityV1TrustStatement = mockProcessIdentityV1TrustStatement,
            fetchVcSchemaTrustStatus = mockFetchVcSchemaTrustStatus,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `FetchTrustForIssuance runs specific things`() = runTest {
        val result = useCase(
            issuerDid = ISSUER_DID,
            vcSchemaId = VC_SCHEMA_ID,
        )

        assertEquals(mockIdentityTrustStatement, result.actorTrustStatement)

        coVerifyOrder {
            mockGetActorEnvironment(
                credentialIssuer = ISSUER_DID,
            )
            mockProcessIdentityV1TrustStatement(ISSUER_DID)
            mockFetchVcSchemaTrustStatus(
                trustStatementActor = TrustStatementActor.ISSUER,
                actorDid = ISSUER_DID,
                vcSchemaId = VC_SCHEMA_ID,
            )
        }
    }

    @Test
    fun `FetchTrustForIssuance uses identityV1 trust statement if feature flag is set`() = runTest {
        useCase(
            issuerDid = ISSUER_DID,
            vcSchemaId = VC_SCHEMA_ID,
        )

        coVerify(exactly = 1) {
            mockProcessIdentityV1TrustStatement(ISSUER_DID)
            mockFetchVcSchemaTrustStatus(
                trustStatementActor = TrustStatementActor.ISSUER,
                actorDid = ISSUER_DID,
                vcSchemaId = VC_SCHEMA_ID,
            )
        }
    }

    @ParameterizedTest
    @MethodSource("fetchTrustInputs")
    fun `FetchTrustForIssuance fetches identityV1 trust statement only for swiyu ecosystem issuers`(
        actorEnvironment: ActorEnvironment,
    ) = runTest {
        coEvery { mockGetActorEnvironment(ISSUER_DID) } returns actorEnvironment

        useCase(
            issuerDid = ISSUER_DID,
            vcSchemaId = VC_SCHEMA_ID,
        )

        coVerify(exactly = 1) {
            mockProcessIdentityV1TrustStatement(ISSUER_DID)
            mockFetchVcSchemaTrustStatus(
                trustStatementActor = TrustStatementActor.ISSUER,
                actorDid = ISSUER_DID,
                vcSchemaId = VC_SCHEMA_ID,
            )
        }
    }

    @ParameterizedTest
    @MethodSource("dontFetchTrustInputs")
    fun `FetchTrustForIssuance does not fetch identityV1 trust statement for not swiyu ecosystem issuers`(
        actorEnvironment: ActorEnvironment,
    ) = runTest {
        coEvery { mockGetActorEnvironment(ISSUER_DID) } returns actorEnvironment

        useCase(
            issuerDid = ISSUER_DID,
            vcSchemaId = VC_SCHEMA_ID,
        )

        coVerify(exactly = 0) {
            mockProcessIdentityV1TrustStatement(ISSUER_DID)
        }
    }

    @Test
    fun `FetchTrustForIssuance fetches identity and issuance trust independently`() = runTest {
        setupDefaultMocks()

        val exception = IllegalStateException("fetching trust failed")
        coEvery {
            mockProcessIdentityV1TrustStatement(ISSUER_DID)
        } returns Err(TrustRegistryError.Unexpected(exception))

        coEvery {
            mockFetchVcSchemaTrustStatus(TrustStatementActor.ISSUER, ISSUER_DID, VC_SCHEMA_ID)
        } returns Ok(VcSchemaTrustStatus.TRUSTED)

        val trustResult = useCase(
            issuerDid = ISSUER_DID,
            vcSchemaId = VC_SCHEMA_ID,
        )

        val expectedTrustCheckResult = TrustCheckResult(
            actorEnvironment = ActorEnvironment.PRODUCTION,
            actorTrustStatement = null,
            vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
        )

        assertEquals(expectedTrustCheckResult, trustResult)

        coVerifyOrder {
            mockProcessIdentityV1TrustStatement(ISSUER_DID)
            mockFetchVcSchemaTrustStatus(any(), any(), any())
        }
    }

    private fun setupDefaultMocks() {
        coEvery { mockGetActorEnvironment(ISSUER_DID) } returns ActorEnvironment.PRODUCTION

        coEvery {
            mockProcessIdentityV1TrustStatement(ISSUER_DID)
        } returns Ok(mockIdentityTrustStatement)

        coEvery {
            mockFetchVcSchemaTrustStatus(TrustStatementActor.ISSUER, ISSUER_DID, VC_SCHEMA_ID)
        } returns Ok(VcSchemaTrustStatus.TRUSTED)
    }

    private companion object {
        @JvmStatic
        fun fetchTrustInputs(): Stream<Arguments> = Stream.of(
            Arguments.of(ActorEnvironment.PRODUCTION),
            Arguments.of(ActorEnvironment.BETA),
        )

        @JvmStatic
        fun dontFetchTrustInputs(): Stream<Arguments> = Stream.of(
            Arguments.of(ActorEnvironment.EXTERNAL),
        )

        const val ISSUER_DID = "issuer did"
        const val VC_SCHEMA_ID = "vcSchemaId"

        val orgNames = mapOf(
            "en" to "issuer name en",
            "de" to "issuer name de",
        )
    }
}
