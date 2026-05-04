package ch.admin.foitt.wallet.platform.actorMetadata

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientMetaData
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientName
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.Field
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptor
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptorFormat
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.LogoUri
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationDefinition
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.model.ActorEnvironment
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.usecase.GetActorEnvironment
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorField
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchAndCacheVerifierDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.InitializeActorForScope
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation.FetchAndCacheVerifierDisplayDataImpl
import ch.admin.foitt.wallet.platform.actorMetadata.mock.ActorMetadataMocks.actorComplianceState
import ch.admin.foitt.wallet.platform.actorMetadata.mock.ActorMetadataMocks.nonComplianceData
import ch.admin.foitt.wallet.platform.actorMetadata.mock.ActorMetadataMocks.nonComplianceReasons
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.FetchNonComplianceData
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV1TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementActor
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.FetchVcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityV1TrustStatement
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
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

class FetchAndCacheVerifierDisplayDataImplTest {
    @MockK
    private lateinit var mockGetActorEnvironment: GetActorEnvironment

    @MockK
    private lateinit var mockProcessIdentityV1TrustStatement: ProcessIdentityV1TrustStatement

    @MockK
    private lateinit var mockFetchVcSchemaTrustStatus: FetchVcSchemaTrustStatus

    @MockK
    private lateinit var mockFetchNonComplianceData: FetchNonComplianceData

    @MockK
    private lateinit var mockInitializeActorForScope: InitializeActorForScope

    @MockK
    private lateinit var mockAuthorizationRequest: AuthorizationRequest

    @MockK
    private lateinit var mockIdentityTrustStatement: IdentityV1TrustStatement

    @MockK
    private lateinit var mockPresentationDefinition: PresentationDefinition

    @MockK
    private lateinit var mockInputDescriptor: InputDescriptor

    @MockK
    private lateinit var mockInputDescriptorFormat: InputDescriptorFormat

    @MockK
    private lateinit var mockField: Field

    private lateinit var useCase: FetchAndCacheVerifierDisplayData

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = FetchAndCacheVerifierDisplayDataImpl(
            getActorEnvironment = mockGetActorEnvironment,
            processIdentityV1TrustStatement = mockProcessIdentityV1TrustStatement,
            fetchVcSchemaTrustStatus = mockFetchVcSchemaTrustStatus,
            fetchNonComplianceData = mockFetchNonComplianceData,
            initializeActorForScope = mockInitializeActorForScope,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Fetching and caching the verifier display data is following specific steps`(): Unit = runTest {
        useCase(
            authorizationRequest = mockAuthorizationRequest,
        )

        val expectedActorDisplayData = ActorDisplayData(
            name = mockTrustedNamesDisplay,
            image = mockMetadataLogoDisplays,
            trustStatus = TrustStatus.TRUSTED,
            vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
            preferredLanguage = null,
            actorType = ActorType.VERIFIER,
            actorComplianceState = actorComplianceState,
            nonComplianceReason = nonComplianceReasons,
        )

        coVerifyOrder {
            mockProcessIdentityV1TrustStatement(clientId)
            mockFetchVcSchemaTrustStatus(TrustStatementActor.VERIFIER, clientId, vcSchemaId)
            mockInitializeActorForScope(expectedActorDisplayData, componentScope = ComponentScope.Verifier)
        }
    }

    @Test
    fun `A valid trust statement from our prod ecosystem will display as trusted`(): Unit = runTest {
        useCase(
            authorizationRequest = mockAuthorizationRequest,
        )

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerifyOrder {
            mockInitializeActorForScope.invoke(
                actorDisplayData = capture(capturedDisplayData),
                componentScope = ComponentScope.Verifier
            )
        }

        assertEquals(TrustStatus.TRUSTED, capturedDisplayData.captured.trustStatus)
    }

    @Test
    fun `A valid trust statement from our beta ecosystem will display as trusted`(): Unit = runTest {
        coEvery { mockGetActorEnvironment(any()) } returns ActorEnvironment.BETA

        useCase(
            authorizationRequest = mockAuthorizationRequest,
        )

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerifyOrder {
            mockInitializeActorForScope.invoke(
                actorDisplayData = capture(capturedDisplayData),
                componentScope = ComponentScope.Verifier
            )
        }

        assertEquals(TrustStatus.TRUSTED, capturedDisplayData.captured.trustStatus)
    }

    @Test
    fun `An invalid trust statement from our prod ecosystem will display as not trusted`(): Unit = runTest {
        coEvery { mockProcessIdentityV1TrustStatement(clientId) } returns trustRegistryError

        useCase(
            authorizationRequest = mockAuthorizationRequest,
        )

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerifyOrder {
            mockInitializeActorForScope.invoke(
                actorDisplayData = capture(capturedDisplayData),
                componentScope = ComponentScope.Verifier
            )
        }

        assertEquals(TrustStatus.NOT_TRUSTED, capturedDisplayData.captured.trustStatus)
    }

    @Test
    fun `An invalid trust statement from our beta ecosystem will display as not trusted`(): Unit = runTest {
        coEvery { mockGetActorEnvironment(any()) } returns ActorEnvironment.BETA
        coEvery { mockProcessIdentityV1TrustStatement(clientId) } returns trustRegistryError

        useCase(
            authorizationRequest = mockAuthorizationRequest,
        )

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerifyOrder {
            mockInitializeActorForScope.invoke(
                actorDisplayData = capture(capturedDisplayData),
                componentScope = ComponentScope.Verifier
            )
        }

        assertEquals(TrustStatus.NOT_TRUSTED, capturedDisplayData.captured.trustStatus)
    }

    @Test
    fun `A valid trust statement not from our ecosystem will display as external`(): Unit = runTest {
        coEvery { mockGetActorEnvironment(any()) } returns ActorEnvironment.EXTERNAL

        useCase(
            authorizationRequest = mockAuthorizationRequest,
        )

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerifyOrder {
            mockInitializeActorForScope.invoke(
                actorDisplayData = capture(capturedDisplayData),
                componentScope = ComponentScope.Verifier
            )
        }

        assertEquals(TrustStatus.EXTERNAL, capturedDisplayData.captured.trustStatus)
    }

    @Test
    fun `An invalid trust statement not from our ecosystem will display as external`(): Unit = runTest {
        coEvery { mockGetActorEnvironment(any()) } returns ActorEnvironment.EXTERNAL
        coEvery { mockProcessIdentityV1TrustStatement(clientId) } returns trustRegistryError

        useCase(
            authorizationRequest = mockAuthorizationRequest,
        )

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerifyOrder {
            mockInitializeActorForScope.invoke(
                actorDisplayData = capture(capturedDisplayData),
                componentScope = ComponentScope.Verifier
            )
        }

        assertEquals(TrustStatus.EXTERNAL, capturedDisplayData.captured.trustStatus)
    }

    @Test
    fun `A non-trusted vcSchema will display as not trusted`(): Unit = runTest {
        coEvery {
            mockFetchVcSchemaTrustStatus(TrustStatementActor.VERIFIER, clientId, vcSchemaId)
        } returns Ok(VcSchemaTrustStatus.NOT_TRUSTED)

        useCase(
            authorizationRequest = mockAuthorizationRequest,
        )

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerify {
            mockInitializeActorForScope.invoke(
                actorDisplayData = capture(capturedDisplayData),
                componentScope = ComponentScope.Verifier
            )
        }

        assertEquals(VcSchemaTrustStatus.NOT_TRUSTED, capturedDisplayData.captured.vcSchemaTrustStatus)
    }

    @Test
    fun `A unprotected vcSchema will display as unprotected`(): Unit = runTest {
        coEvery {
            mockFetchVcSchemaTrustStatus(TrustStatementActor.VERIFIER, clientId, vcSchemaId)
        } returns Ok(VcSchemaTrustStatus.UNPROTECTED)

        useCase(
            authorizationRequest = mockAuthorizationRequest,
        )

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerify {
            mockInitializeActorForScope.invoke(
                actorDisplayData = capture(capturedDisplayData),
                componentScope = ComponentScope.Verifier
            )
        }

        assertEquals(VcSchemaTrustStatus.UNPROTECTED, capturedDisplayData.captured.vcSchemaTrustStatus)
    }

    @Test
    fun `VcSchema trust only fetched when the schemaId is available`() = runTest {
        every { mockField.path } returns listOf("other path")

        useCase(
            authorizationRequest = mockAuthorizationRequest,
        )

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerify {
            mockInitializeActorForScope.invoke(
                actorDisplayData = capture(capturedDisplayData),
                componentScope = ComponentScope.Verifier
            )
        }

        coVerify(exactly = 0) {
            mockFetchVcSchemaTrustStatus(any(), any(), any())
        }

        assertEquals(VcSchemaTrustStatus.UNPROTECTED, capturedDisplayData.captured.vcSchemaTrustStatus)
    }

    @Test
    fun `Valid trust statement data is shown first`(): Unit = runTest {
        useCase(
            authorizationRequest = mockAuthorizationRequest,
        )

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerifyOrder {
            mockInitializeActorForScope.invoke(actorDisplayData = capture(capturedDisplayData), ComponentScope.Verifier)
        }

        assertEquals(mockTrustedNamesDisplay, capturedDisplayData.captured.name)
        // logo of the trust statement is ignored for now -> metadata logo is used instead
        assertEquals(mockMetadataLogoDisplays, capturedDisplayData.captured.image)
    }

    @Test
    fun `In case of invalid trust statement, falls back to the presentation request metadata`(): Unit = runTest {
        coEvery { mockProcessIdentityV1TrustStatement.invoke(did = any()) } returns trustRegistryError
        useCase(
            authorizationRequest = mockAuthorizationRequest,
        )

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerifyOrder {
            mockInitializeActorForScope.invoke(actorDisplayData = capture(capturedDisplayData), any())
        }

        assertEquals(mockMetadataNameDisplays, capturedDisplayData.captured.name)
        assertEquals(mockMetadataLogoDisplays, capturedDisplayData.captured.image)
    }

    @Test
    fun `Fetching and caching verifier display data fetches identity and verification trust independently`() = runTest {
        setupDefaultMocks()

        val exception = IllegalStateException("fetching trust failed")
        coEvery {
            mockProcessIdentityV1TrustStatement(clientId)
        } returns Err(TrustRegistryError.Unexpected(exception))

        coEvery {
            mockFetchVcSchemaTrustStatus(TrustStatementActor.VERIFIER, clientId, vcSchemaId)
        } returns Ok(VcSchemaTrustStatus.TRUSTED)

        useCase(
            authorizationRequest = mockAuthorizationRequest,
        )

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerifyOrder {
            mockInitializeActorForScope.invoke(actorDisplayData = capture(capturedDisplayData), any())
        }

        assertEquals(TrustStatus.NOT_TRUSTED, capturedDisplayData.captured.trustStatus)
        assertEquals(VcSchemaTrustStatus.TRUSTED, capturedDisplayData.captured.vcSchemaTrustStatus)
    }

    private fun setupDefaultMocks() {
        every { mockAuthorizationRequest.clientId } returns clientId
        every { mockAuthorizationRequest.clientMetaData } returns mockClientMetadata
        every { mockAuthorizationRequest.presentationDefinition } returns mockPresentationDefinition

        every { mockPresentationDefinition.inputDescriptors } returns listOf(mockInputDescriptor)

        every { mockInputDescriptor.formats } returns listOf(mockInputDescriptorFormat)
        every { mockInputDescriptor.constraints.fields } returns listOf(mockField)

        every { mockField.path } returns listOf("$.vct")
        every { mockField.filter?.const } returns vcSchemaId

        every { mockIdentityTrustStatement.entityName } returns mockTrustedNames

        coEvery { mockGetActorEnvironment(any()) } returns ActorEnvironment.PRODUCTION

        coEvery { mockProcessIdentityV1TrustStatement(clientId) } returns Ok(mockIdentityTrustStatement)

        coEvery {
            mockFetchVcSchemaTrustStatus(TrustStatementActor.VERIFIER, clientId, vcSchemaId)
        } returns Ok(VcSchemaTrustStatus.TRUSTED)

        coEvery { mockFetchNonComplianceData(clientId) } returns nonComplianceData

        coEvery {
            mockInitializeActorForScope.invoke(any(), componentScope = ComponentScope.Verifier)
        } just runs
    }
    //region mock data

    private val clientId = "clientId1"
    private val vcSchemaId = "vcSchemaId"

    private val trustRegistryError = Err(TrustRegistryError.Unexpected(IllegalStateException("error")))

    private val mockTrustedNames = mapOf(
        "de-de" to "name DeDe",
        "en-gb" to "name EnGb"
    )

    private val mockTrustedNamesDisplay = mockTrustedNames.entries.map { entry ->
        ActorField(value = entry.value, locale = entry.key)
    }

    private val mockClientMetadata = ClientMetaData(
        clientNameList = listOf(
            ClientName("clientName En", locale = "en-gb")
        ),
        logoUriList = listOf(
            LogoUri("logoUri De", locale = "de-de")
        ),
    )

    private val mockMetadataNameDisplays = mockClientMetadata.clientNameList.map { entry ->
        ActorField(
            value = entry.clientName,
            locale = entry.locale,
        )
    }
    private val mockMetadataLogoDisplays = mockClientMetadata.logoUriList.map { entry ->
        ActorField(
            value = entry.logoUri,
            locale = entry.locale,
        )
    }
    //endregion
}
