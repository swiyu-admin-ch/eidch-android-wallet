package ch.admin.foitt.wallet.platform.trustRegistry

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.model.ActorEnvironment
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.usecase.GetActorEnvironment
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV1TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityV1TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateIdentityTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ProcessIdentityTrustStatementImpl
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import ch.admin.foitt.wallet.util.assertOkNullable
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
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class ProcessIdentityTrustStatementImplTest {
    @MockK
    private lateinit var mockValidateIdentityTrustStatement: ValidateIdentityTrustStatement

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    @MockK
    private lateinit var mockGetActorEnvironment: GetActorEnvironment

    @MockK
    private lateinit var mockProcessIdentityV1TrustStatement: ProcessIdentityV1TrustStatement

    @MockK
    private lateinit var mockIdentityTrustStatementJwt: Jwt

    @MockK
    private lateinit var mockIdentityV2TrustStatement: IdentityV2TrustStatement

    @MockK
    private lateinit var mockIdentityV1TrustStatement: IdentityV1TrustStatement

    private lateinit var useCase: ProcessIdentityTrustStatement

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = ProcessIdentityTrustStatementImpl(
            validateIdentityTrustStatement = mockValidateIdentityTrustStatement,
            environmentSetupRepository = mockEnvironmentSetupRepository,
            getActorEnvironment = mockGetActorEnvironment,
            processIdentityV1TrustStatement = mockProcessIdentityV1TrustStatement,
        )

        coEvery {
            mockValidateIdentityTrustStatement(
                trustStatement = mockIdentityTrustStatementJwt,
                actorDid = ACTOR_DID,
            )
        } returns Ok(mockIdentityV2TrustStatement)

        coEvery { mockEnvironmentSetupRepository.terminateOnInvalidIdTSEnabled } returns true

        coEvery { mockGetActorEnvironment(ACTOR_DID) } returns ActorEnvironment.PRODUCTION

        coEvery {
            mockProcessIdentityV1TrustStatement(did = ACTOR_DID)
        } returns Ok(mockIdentityV1TrustStatement)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A IdentityV2 trust statement is correctly processed`() = runTest {
        val result = useCase(
            identityTrustStatementJwt = mockIdentityTrustStatementJwt,
            actorDid = ACTOR_DID,
        ).assertOk()

        assertEquals(mockIdentityV2TrustStatement, result)
    }

    @Test
    fun `Processing an identity v2 trust statement maps validation errors`() = runTest {
        val exception = IllegalStateException("trust error")
        coEvery {
            mockValidateIdentityTrustStatement(any(), any())
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(
            identityTrustStatementJwt = mockIdentityTrustStatementJwt,
            actorDid = ACTOR_DID,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Without a v2 identity statement and disabled feature flag, do not check v1 exists and return null`() = runTest {
        coEvery { mockEnvironmentSetupRepository.terminateOnInvalidIdTSEnabled } returns false

        val result = useCase(
            identityTrustStatementJwt = null,
            actorDid = ACTOR_DID,
        ).assertOkNullable()

        assertNull(result)

        coVerify(exactly = 0) { mockProcessIdentityV1TrustStatement(did = any()) }
    }

    @Test
    fun `Without a v2 identity statement, make sure a valid v1 exists and return null`() = runTest {
        val result = useCase(
            identityTrustStatementJwt = null,
            actorDid = ACTOR_DID,
        ).assertOkNullable()

        assertNull(result)

        coVerify { mockProcessIdentityV1TrustStatement(did = ACTOR_DID) }
    }

    @ParameterizedTest
    @EnumSource(ActorEnvironment::class, names = ["PRODUCTION", "BETA"])
    fun `Without a v2 identity statement, v1 is only checked for PROD and BETA envs`(input: ActorEnvironment) = runTest {
        coEvery { mockGetActorEnvironment(ACTOR_DID) } returns input

        useCase(
            identityTrustStatementJwt = null,
            actorDid = ACTOR_DID,
        ).assertOkNullable()

        coVerify { mockProcessIdentityV1TrustStatement(any()) }
    }

    @Test
    fun `Without a v2 identity statement, and error during v1 returns an error`() = runTest {
        val exception = IllegalStateException("trust error")
        coEvery { mockProcessIdentityV1TrustStatement(any()) } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(
            identityTrustStatementJwt = null,
            actorDid = ACTOR_DID,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Without a v2 identity statement, v1 is not checked for EXTERNAL and returns an error`() = runTest {
        coEvery { mockGetActorEnvironment(ACTOR_DID) } returns ActorEnvironment.EXTERNAL

        useCase(
            identityTrustStatementJwt = null,
            actorDid = ACTOR_DID,
        ).assertErrorType(TrustRegistryError.UnknownRegistry::class)

        coVerify(exactly = 0) { mockProcessIdentityV1TrustStatement(any()) }
    }

    private companion object Companion {
        const val ACTOR_DID = "actor did"
    }
}
