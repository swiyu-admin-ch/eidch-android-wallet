package ch.admin.foitt.wallet.feature.presentationRequest

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseConfig
import ch.admin.foitt.openid4vc.domain.usecase.GetAuthorizationResponseConfig
import ch.admin.foitt.openid4vc.domain.usecase.SubmitAnyCredentialNetworkPresentation
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationRequestError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.SubmitPresentation
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.implementation.SubmitPresentationImpl
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProximitySubmissionError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.VerificationProcessType
import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemEntity
import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemWithKeyBinding
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithBundleItemsWithKeyBinding
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.proximity.domain.model.domain.repository.ProximityRepository
import ch.admin.foitt.wallet.platform.proximity.domain.usecase.GetProximityRepositoryForScope
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithBundleItemsWithKeyBindingRepository
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
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError as OpenIdPresentationRequestError

class SubmitPresentationImplTest {

    @MockK
    private lateinit var mockSubmitAnyCredentialNetworkPresentation: SubmitAnyCredentialNetworkPresentation

    @MockK
    private lateinit var mockGetAuthorizationResponseConfig: GetAuthorizationResponseConfig

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    @MockK
    private lateinit var mockVerifiableCredentialWithBundleItemsWithKeyBindingRepository:
        VerifiableCredentialWithBundleItemsWithKeyBindingRepository

    @MockK
    private lateinit var mockVerifiableCredentialRepository: VerifiableCredentialRepository

    @MockK
    private lateinit var mockBundleItemRepository: BundleItemRepository

    @MockK
    private lateinit var mockGetProximityRepositoryForScope: GetProximityRepositoryForScope

    @MockK
    private lateinit var mockProximityRepository: ProximityRepository

    @MockK
    private lateinit var mockAuthorizationRequest: AuthorizationRequest

    @MockK
    private lateinit var mockAuthorizationResponseConfig: AuthorizationResponseConfig

    private lateinit var presentationRequestWithRawNetwork: PresentationRequestWithRaw
    private lateinit var presentationRequestWithRawProximity: PresentationRequestWithRaw

    private lateinit var submitPresentationUseCase: SubmitPresentation

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every {
            mockGetProximityRepositoryForScope()
        } returns mockProximityRepository

        presentationRequestWithRawNetwork = PresentationRequestWithRaw(
            authorizationRequest = mockAuthorizationRequest,
            rawPresentationRequest = RAW_PRESENTATION_REQUEST,
            verificationProcessType = VerificationProcessType.NETWORK,
        )

        presentationRequestWithRawProximity = PresentationRequestWithRaw(
            authorizationRequest = mockAuthorizationRequest,
            rawPresentationRequest = RAW_PRESENTATION_REQUEST,
            verificationProcessType = VerificationProcessType.PROXIMITY,
        )

        submitPresentationUseCase = SubmitPresentationImpl(
            environmentSetupRepository = mockEnvironmentSetupRepository,
            verifiableCredentialWithBundleItemsWithKeyBindingRepository =
            mockVerifiableCredentialWithBundleItemsWithKeyBindingRepository,
            verifiableCredentialRepository = mockVerifiableCredentialRepository,
            bundleItemRepository = mockBundleItemRepository,
            submitAnyCredentialNetworkPresentation = mockSubmitAnyCredentialNetworkPresentation,
            getAuthorizationResponseConfig = mockGetAuthorizationResponseConfig,
            getProximityRepositoryForScope = mockGetProximityRepositoryForScope,
        )

        every { mockEnvironmentSetupRepository.payloadEncryptionEnabled } returns true
        coEvery {
            mockVerifiableCredentialWithBundleItemsWithKeyBindingRepository.getByCredentialId(CREDENTIAL_ID)
        } returns Ok(createCredentialWithBundleItems())

        coEvery {
            mockGetAuthorizationResponseConfig(
                anyCredential = any(),
                presentationPaths = presentationPaths,
                authorizationRequest = mockAuthorizationRequest,
                usePayloadEncryption = true,
                dcqlQueryId = any(),
            )
        } returns Ok(mockAuthorizationResponseConfig)

        coEvery {
            mockSubmitAnyCredentialNetworkPresentation(
                authorizationRequest = mockAuthorizationRequest,
                authorizationResponseConfig = mockAuthorizationResponseConfig,
            )
        } returns Ok(Unit)

        coEvery { mockProximityRepository.submit(mockAuthorizationResponseConfig) } returns Ok(Unit)

        coEvery { mockBundleItemRepository.onPresented(CREDENTIAL_ID, BUNDLE_ITEM_ID) } returns Ok(NEXT_BUNDLE_ITEM_ID)
        coEvery {
            mockVerifiableCredentialRepository.updateNextBundleIdByCredentialId(
                credentialId = CREDENTIAL_ID,
                nextPresentableBundleItemId = NEXT_BUNDLE_ITEM_ID
            )
        } returns Ok(1)

        coEvery { mockBundleItemRepository.onPresented(any(), any()) } returns Ok(NEXT_BUNDLE_ITEM_ID)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Submitting network presentation updates bundle id after successful submission`() = runTest {
        submitPresentationUseCase(presentationRequestWithRawNetwork, compatibleCredentialWithDcql).assertOk()

        coVerify(exactly = 1) {
            mockGetAuthorizationResponseConfig(
                anyCredential = any(),
                presentationPaths = presentationPaths,
                authorizationRequest = mockAuthorizationRequest,
                usePayloadEncryption = true,
                dcqlQueryId = DCQL_QUERY_ID,
            )
        }
        coVerify(exactly = 1) {
            mockSubmitAnyCredentialNetworkPresentation(
                authorizationRequest = mockAuthorizationRequest,
                authorizationResponseConfig = mockAuthorizationResponseConfig,
            )
        }
        coVerify(exactly = 1) { mockBundleItemRepository.onPresented(CREDENTIAL_ID, BUNDLE_ITEM_ID) }
        coVerify(exactly = 1) {
            mockVerifiableCredentialRepository.updateNextBundleIdByCredentialId(CREDENTIAL_ID, NEXT_BUNDLE_ITEM_ID)
        }
    }

    @Test
    fun `Submitting proximity presentation updates bundle id after successful submission`() = runTest {
        submitPresentationUseCase(presentationRequestWithRawProximity, compatibleCredential).assertOk()

        coVerify(exactly = 1) { mockProximityRepository.submit(mockAuthorizationResponseConfig) }
        coVerify(exactly = 1) { mockBundleItemRepository.onPresented(CREDENTIAL_ID, BUNDLE_ITEM_ID) }
        coVerify(exactly = 1) {
            mockVerifiableCredentialRepository.updateNextBundleIdByCredentialId(CREDENTIAL_ID, NEXT_BUNDLE_ITEM_ID)
        }
    }

    @Test
    fun `Submitting presentation maps credential with key binding repository errors`() = runTest {
        val exception = IllegalStateException("repository failure")
        coEvery { mockVerifiableCredentialWithBundleItemsWithKeyBindingRepository.getByCredentialId(any()) } returns
            Err(SsiError.Unexpected(exception))

        val result = submitPresentationUseCase(presentationRequestWithRawNetwork, compatibleCredential)

        val error = result.assertErrorType(PresentationRequestError.Unexpected::class)
        assertEquals(exception, error.throwable)
        coVerify(exactly = 0) { mockSubmitAnyCredentialNetworkPresentation(any(), any()) }
        coVerify(exactly = 0) { mockBundleItemRepository.onPresented(any(), any()) }
    }

    @Test
    fun `Submitting presentation maps any credential conversion errors`() = runTest {
        coEvery { mockVerifiableCredentialWithBundleItemsWithKeyBindingRepository.getByCredentialId(any()) } returns
            Ok(createCredentialWithBundleItems(format = CredentialFormat.UNKNOWN))

        val result = submitPresentationUseCase(presentationRequestWithRawNetwork, compatibleCredential)

        result.assertErrorType(PresentationRequestError.Unexpected::class)
        coVerify(exactly = 0) { mockSubmitAnyCredentialNetworkPresentation(any(), any()) }
        coVerify(exactly = 0) { mockBundleItemRepository.onPresented(any(), any()) }
    }

    @Test
    fun `Submitting presentation maps get authorization response config errors`() = runTest {
        val exception = IllegalStateException("response config failure")
        coEvery {
            mockGetAuthorizationResponseConfig(
                anyCredential = any(),
                presentationPaths = any(),
                authorizationRequest = any(),
                usePayloadEncryption = any(),
                dcqlQueryId = any(),
            )
        } returns Err(OpenIdPresentationRequestError.Unexpected(exception))

        val result = submitPresentationUseCase(presentationRequestWithRawNetwork, compatibleCredential)

        val error = result.assertErrorType(PresentationRequestError.Unexpected::class)
        assertEquals(exception, error.throwable)
        coVerify(exactly = 0) { mockSubmitAnyCredentialNetworkPresentation(any(), any()) }
    }

    @Test
    fun `Submitting network presentation maps submit errors and still updates bundle id`() = runTest {
        coEvery {
            mockSubmitAnyCredentialNetworkPresentation(any(), any())
        } returns Err(OpenIdPresentationRequestError.NetworkError)

        val result = submitPresentationUseCase(presentationRequestWithRawNetwork, compatibleCredential)

        result.assertErrorType(PresentationRequestError.NetworkError::class)
        coVerify(exactly = 1) { mockBundleItemRepository.onPresented(CREDENTIAL_ID, BUNDLE_ITEM_ID) }
        coVerify(exactly = 1) {
            mockVerifiableCredentialRepository.updateNextBundleIdByCredentialId(CREDENTIAL_ID, NEXT_BUNDLE_ITEM_ID)
        }
    }

    @Test
    fun `Submitting proximity presentation maps submit errors and still updates bundle id`() = runTest {
        coEvery { mockProximityRepository.submit(any()) } returns Err(ProximitySubmissionError.Failed())

        val result = submitPresentationUseCase(presentationRequestWithRawProximity, compatibleCredential)

        result.assertErrorType(PresentationRequestError.Unexpected::class)
        coVerify(exactly = 1) { mockBundleItemRepository.onPresented(CREDENTIAL_ID, BUNDLE_ITEM_ID) }
        coVerify(exactly = 1) {
            mockVerifiableCredentialRepository.updateNextBundleIdByCredentialId(CREDENTIAL_ID, NEXT_BUNDLE_ITEM_ID)
        }
    }

    @Test
    fun `Submitting presentation returns presentation request error when submit fails even if bundle update fails`() = runTest {
        val exception = IllegalStateException("bundle update failure")
        coEvery { mockSubmitAnyCredentialNetworkPresentation(any(), any()) } returns
            Err(OpenIdPresentationRequestError.VerificationError)
        coEvery { mockBundleItemRepository.onPresented(any(), any()) } returns Err(SsiError.Unexpected(exception))

        val result = submitPresentationUseCase(presentationRequestWithRawNetwork, compatibleCredential)
        result.assertErrorType(PresentationRequestError.VerificationError::class)
    }

    @Test
    fun `Submitting presentation returns presentation request error when submit fails even if credential update fails`() = runTest {
        val exception = IllegalStateException("credential update failure")
        coEvery { mockSubmitAnyCredentialNetworkPresentation(any(), any()) } returns
            Err(OpenIdPresentationRequestError.VerificationError)
        coEvery {
            mockVerifiableCredentialRepository.updateNextBundleIdByCredentialId(any(), any())
        } returns Err(SsiError.Unexpected(exception))

        val result = submitPresentationUseCase(presentationRequestWithRawNetwork, compatibleCredential)
        result.assertErrorType(PresentationRequestError.VerificationError::class)
    }

    @Test
    fun `Submitting presentation returns bundle update error after successful submit`() = runTest {
        val exception = IllegalStateException("bundle update failure")
        coEvery { mockBundleItemRepository.onPresented(any(), any()) } returns Err(SsiError.Unexpected(exception))

        val result = submitPresentationUseCase(presentationRequestWithRawNetwork, compatibleCredential)

        val error = result.assertErrorType(PresentationRequestError.Unexpected::class)
        assertEquals(exception, error.throwable)
        coVerify(exactly = 0) { mockVerifiableCredentialRepository.updateNextBundleIdByCredentialId(any(), any()) }
    }

    @Test
    fun `Submitting presentation returns credential update error after successful submit`() = runTest {
        val exception = IllegalStateException("credential update failure")
        coEvery {
            mockVerifiableCredentialRepository.updateNextBundleIdByCredentialId(any(), any())
        } returns Err(SsiError.Unexpected(exception))

        val result = submitPresentationUseCase(presentationRequestWithRawNetwork, compatibleCredential)

        val error = result.assertErrorType(PresentationRequestError.Unexpected::class)
        assertEquals(exception, error.throwable)
    }

    private fun createCredentialWithBundleItems(
        format: CredentialFormat = CredentialFormat.VC_SD_JWT
    ) = VerifiableCredentialWithBundleItemsWithKeyBinding(
        credential = Credential(
            id = CREDENTIAL_ID,
            format = format,
            issuerUrl = URL(CREDENTIAL_ISSUER),
        ),
        verifiableCredential = VerifiableCredentialEntity(
            credentialId = CREDENTIAL_ID,
            issuer = "issuer",
            validFrom = null,
            validUntil = null,
            nextPresentableBundleItemId = BUNDLE_ITEM_ID
        ),
        bundleItemsWithKeyBinding = listOf(
            BundleItemWithKeyBinding(
                bundleItem = BundleItemEntity(
                    id = BUNDLE_ITEM_ID,
                    credentialId = CREDENTIAL_ID,
                    payload = VALID_SD_JWT_PAYLOAD
                ),
                keyBinding = null
            )
        ),
    )

    private companion object {
        const val FIELD_KEY_1 = "fieldKey1"
        const val FIELD_KEY_2 = "fieldKey2"
        const val RAW_PRESENTATION_REQUEST = "rawPresentationRequest"
        val path1 = listOf(ClaimsPathPointerComponent.String(FIELD_KEY_1))
        val path2 = listOf(ClaimsPathPointerComponent.String(FIELD_KEY_2))
        val presentationPaths = listOf(path1, path2)

        const val CREDENTIAL_ID = 1L
        const val BUNDLE_ITEM_ID = 11L
        const val NEXT_BUNDLE_ITEM_ID = 22L
        const val CREDENTIAL_ISSUER = "https://issuer.example"
        const val DCQL_QUERY_ID = "query-id-1"
        const val VALID_SD_JWT_PAYLOAD =
            "eyJhbGciOiJFUzI1NiIsImtpZCI6InRlc3Qta2lkIn0." +
                "eyJpc3MiOiJodHRwczovL2lzc3Vlci5leGFtcGxlIiwidmN0IjoidGVzdC12Y3QiLCJfc2QiOltdLCJfc2RfYWxnIjoic2hhLTI1NiJ9." +
                "c2ln~"
        val compatibleCredential = CompatibleCredential(CREDENTIAL_ID, presentationPaths, "1")
        val compatibleCredentialWithDcql =
            CompatibleCredential(CREDENTIAL_ID, presentationPaths, dcqlQueryId = DCQL_QUERY_ID)
    }
}
