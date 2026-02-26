package ch.admin.foitt.wallet.platform.actorMetadata

import ch.admin.foitt.wallet.platform.actorEnvironment.domain.model.ActorEnvironment
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorField
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.CacheIssuerDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.InitializeActorForScope
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation.CacheIssuerDisplayDataImpl
import ch.admin.foitt.wallet.platform.actorMetadata.mock.ActorMetadataMocks.nonComplianceData
import ch.admin.foitt.wallet.platform.actorMetadata.mock.ActorMetadataMocks.nonComplianceReasons
import ch.admin.foitt.wallet.platform.actorMetadata.mock.ActorMetadataMocks.nonComplianceState
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyIssuerDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV1TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustCheckResult
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CacheIssuerDisplayDataImplTest {

    @MockK
    private lateinit var mockInitializeActorForScope: InitializeActorForScope

    @MockK
    private lateinit var mockIdentityTrustStatement: IdentityV1TrustStatement

    private lateinit var useCase: CacheIssuerDisplayData

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = CacheIssuerDisplayDataImpl(
            initializeActorForScope = mockInitializeActorForScope,
        )

        coEvery { mockIdentityTrustStatement.entityName } returns mockTrustedNames

        coEvery {
            mockInitializeActorForScope.invoke(any(), componentScope = ComponentScope.CredentialIssuer)
        } just runs
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Caching the issuer display data for existing trust statement and vcSchema trust status creates the correct data`(): Unit = runTest {
        val trustCheckResult = TrustCheckResult(
            actorTrustStatement = mockIdentityTrustStatement,
            vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
            actorEnvironment = ActorEnvironment.PRODUCTION,
        )

        useCase(trustCheckResult, credentialIssuerDisplays, nonComplianceData)

        val expectedActorDisplayData = ActorDisplayData(
            name = mockMetadataNameDisplays,
            image = mockMetadataLogoDisplays,
            trustStatus = TrustStatus.TRUSTED,
            vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
            preferredLanguage = null,
            actorType = ActorType.ISSUER,
            nonComplianceState = nonComplianceState,
            nonComplianceReason = nonComplianceReasons,
        )

        coVerify {
            mockInitializeActorForScope.invoke(expectedActorDisplayData, componentScope = ComponentScope.CredentialIssuer)
        }
    }

    @Test
    fun `A valid trust statement will display as trusted`(): Unit = runTest {
        val trustCheckResult = TrustCheckResult(
            actorTrustStatement = mockIdentityTrustStatement,
            vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
            actorEnvironment = ActorEnvironment.PRODUCTION,
        )

        useCase(trustCheckResult, credentialIssuerDisplays, nonComplianceData)

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerify {
            mockInitializeActorForScope.invoke(actorDisplayData = capture(capturedDisplayData), any())
        }

        assertEquals(TrustStatus.TRUSTED, capturedDisplayData.captured.trustStatus)
    }

    @Test
    fun `An invalid trust statement will display as not trusted`(): Unit = runTest {
        val trustCheckResult = TrustCheckResult(
            actorTrustStatement = null,
            vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
            actorEnvironment = ActorEnvironment.PRODUCTION,
        )

        useCase(trustCheckResult, credentialIssuerDisplays, nonComplianceData)

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerify {
            mockInitializeActorForScope.invoke(actorDisplayData = capture(capturedDisplayData), any())
        }

        assertEquals(TrustStatus.NOT_TRUSTED, capturedDisplayData.captured.trustStatus)
    }

    @Test
    fun `A non-trusted vcSchema will display as not trusted`(): Unit = runTest {
        val trustCheckResult = TrustCheckResult(
            actorTrustStatement = mockIdentityTrustStatement,
            vcSchemaTrustStatus = VcSchemaTrustStatus.NOT_TRUSTED,
            actorEnvironment = ActorEnvironment.PRODUCTION,
        )

        useCase(trustCheckResult, credentialIssuerDisplays, nonComplianceData)

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerify {
            mockInitializeActorForScope.invoke(actorDisplayData = capture(capturedDisplayData), any())
        }

        assertEquals(VcSchemaTrustStatus.NOT_TRUSTED, capturedDisplayData.captured.vcSchemaTrustStatus)
    }

    @Test
    fun `A unprotected vcSchema will display as unprotected`(): Unit = runTest {
        val trustCheckResult = TrustCheckResult(
            actorTrustStatement = mockIdentityTrustStatement,
            vcSchemaTrustStatus = VcSchemaTrustStatus.UNPROTECTED,
            actorEnvironment = ActorEnvironment.PRODUCTION,
        )

        useCase(trustCheckResult, credentialIssuerDisplays, nonComplianceData)

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerify {
            mockInitializeActorForScope.invoke(actorDisplayData = capture(capturedDisplayData), any())
        }

        assertEquals(VcSchemaTrustStatus.UNPROTECTED, capturedDisplayData.captured.vcSchemaTrustStatus)
    }

    @Test
    fun `A trusted statement for an actor in our prod ecosystem is displayed correctly`() = runTest {
        val trustCheckResult = TrustCheckResult(
            actorTrustStatement = mockIdentityTrustStatement,
            vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
            actorEnvironment = ActorEnvironment.PRODUCTION,
        )

        useCase(trustCheckResult, credentialIssuerDisplays, nonComplianceData)

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerify {
            mockInitializeActorForScope.invoke(actorDisplayData = capture(capturedDisplayData), any())
        }

        assertEquals(TrustStatus.TRUSTED, capturedDisplayData.captured.trustStatus)
    }

    @Test
    fun `A not trusted statement for an actor in our prod ecosystem is displayed correctly`() = runTest {
        val trustCheckResult = TrustCheckResult(
            actorTrustStatement = null,
            vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
            actorEnvironment = ActorEnvironment.PRODUCTION,
        )

        useCase(trustCheckResult, credentialIssuerDisplays, nonComplianceData)

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerify {
            mockInitializeActorForScope.invoke(actorDisplayData = capture(capturedDisplayData), any())
        }

        assertEquals(TrustStatus.NOT_TRUSTED, capturedDisplayData.captured.trustStatus)
    }

    @Test
    fun `A trusted statement for an actor in our beta ecosystem is displayed correctly`() = runTest {
        val trustCheckResult = TrustCheckResult(
            actorTrustStatement = mockIdentityTrustStatement,
            vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
            actorEnvironment = ActorEnvironment.BETA,
        )

        useCase(trustCheckResult, credentialIssuerDisplays, nonComplianceData)

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerify {
            mockInitializeActorForScope.invoke(actorDisplayData = capture(capturedDisplayData), any())
        }

        assertEquals(TrustStatus.TRUSTED, capturedDisplayData.captured.trustStatus)
    }

    @Test
    fun `A not trusted statement for an actor in our beta ecosystem is displayed correctly`() = runTest {
        val trustCheckResult = TrustCheckResult(
            actorTrustStatement = null,
            vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
            actorEnvironment = ActorEnvironment.BETA,
        )

        useCase(trustCheckResult, credentialIssuerDisplays, nonComplianceData)

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerify {
            mockInitializeActorForScope.invoke(actorDisplayData = capture(capturedDisplayData), any())
        }

        assertEquals(TrustStatus.NOT_TRUSTED, capturedDisplayData.captured.trustStatus)
    }

    @Test
    fun `A trusted statement for an actor not in our ecosystem is displayed correctly`() = runTest {
        val trustCheckResult = TrustCheckResult(
            actorTrustStatement = mockIdentityTrustStatement,
            vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
            actorEnvironment = ActorEnvironment.EXTERNAL,
        )

        useCase(trustCheckResult, credentialIssuerDisplays, nonComplianceData)

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerify {
            mockInitializeActorForScope.invoke(actorDisplayData = capture(capturedDisplayData), any())
        }

        assertEquals(TrustStatus.EXTERNAL, capturedDisplayData.captured.trustStatus)
    }

    @Test
    fun `A not trusted statement for an actor not in our ecosystem is displayed correctly`() = runTest {
        val trustCheckResult = TrustCheckResult(
            actorTrustStatement = null,
            vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
            actorEnvironment = ActorEnvironment.EXTERNAL,
        )

        useCase(trustCheckResult, credentialIssuerDisplays, nonComplianceData)

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerify {
            mockInitializeActorForScope.invoke(actorDisplayData = capture(capturedDisplayData), any())
        }

        assertEquals(TrustStatus.EXTERNAL, capturedDisplayData.captured.trustStatus)
    }

    //region mock data
    private val mockTrustedNames = mapOf(
        "de-de" to "name DeDe",
        "en-gb" to "name EnGb"
    )

    private val credentialIssuerDisplay01 = AnyIssuerDisplay(
        locale = "en-us",
        name = "credentialIssuer01",
        logo = "credentialImage01",
        logoAltText = null,
    )

    private val credentialIssuerDisplay02 = credentialIssuerDisplay01.copy(
        locale = "de-de",
        name = "credentialIssuer02",
        logo = "credentialImage02",
        logoAltText = null,
    )

    private val credentialIssuerDisplays = listOf(
        credentialIssuerDisplay01,
        credentialIssuerDisplay02,
    )

    private val mockMetadataNameDisplays = credentialIssuerDisplays.map { entry ->
        ActorField(
            value = entry.name,
            locale = entry.locale ?: DisplayLanguage.UNKNOWN,
        )
    }

    private val mockMetadataLogoDisplays = credentialIssuerDisplays.mapNotNull { entry ->
        entry.logo?.let {
            ActorField(
                value = entry.logo,
                locale = entry.locale ?: DisplayLanguage.UNKNOWN,
            )
        }
    }
    //endregion
}
