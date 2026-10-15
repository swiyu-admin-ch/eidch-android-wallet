package ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientIdentifier
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityListError
import ch.admin.foitt.wallet.platform.activityList.domain.repository.CredentialActivityRepository
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialActivityEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceMetadata
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceRequest
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceRequestField
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceVerifierMetadata
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceVerifierReportReason
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.SendVerifierNonComplianceReport
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.SubmitNonComplianceReport
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation.mock.NonComplianceMocks.PRESENTATION_REQUEST_JWT
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation.mock.NonComplianceMocks.presentationRequestJson
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialRepository
import ch.admin.foitt.wallet.util.SafeJsonTestInstance.safeJson
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import io.mockk.CapturingSlot
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(UnsafeResultValueAccess::class)
class SendVerifierNonComplianceReportImplTest {

    @MockK
    private lateinit var mockCredentialActivityRepository: CredentialActivityRepository

    @MockK
    private lateinit var mockVerifiableCredentialRepository: VerifiableCredentialRepository

    @MockK
    private lateinit var mockCredentialActivity: CredentialActivityEntity

    @MockK
    private lateinit var mockVerifiableCredential: VerifiableCredentialEntity

    @MockK
    private lateinit var mockAuthorizationRequest: AuthorizationRequest

    @MockK
    private lateinit var mockClientIdentifier: ClientIdentifier

    @MockK
    private lateinit var mockSubmitNonComplianceReport: SubmitNonComplianceReport

    @MockK
    private lateinit var mockGetCurrentAppLocale: GetCurrentAppLocale

    private lateinit var useCase: SendVerifierNonComplianceReport

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = spyk(
            objToCopy = SendVerifierNonComplianceReportImpl(
                credentialActivityRepository = mockCredentialActivityRepository,
                verifiableCredentialRepo = mockVerifiableCredentialRepository,
                safeJson = safeJson,
                getCurrentAppLocale = mockGetCurrentAppLocale,
                submitNonComplianceReport = mockSubmitNonComplianceReport
            ),
            recordPrivateCalls = true
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @TestFactory
    fun `Sending non compliance report for jwt and json presentation request returns Ok`(): List<DynamicTest> = inputStrings().map { input ->
        DynamicTest.dynamicTest("$input should return success") {
            runTest {
                every { mockCredentialActivity.nonComplianceData } returns input

                val (nonComplianceRequest, _) = createNonComplianceRequest(input)

                coEvery {
                    mockSubmitNonComplianceReport(nonComplianceRequest)
                } returns Ok(Unit)

                useCase(ACTIVITY_ID, reason, DESCRIPTION, CONTEXT, EMAIL).assertOk()

                coVerify {
                    mockCredentialActivityRepository.getById(ACTIVITY_ID)
                    mockVerifiableCredentialRepository.getById(CREDENTIAL_ID)
                    mockSubmitNonComplianceReport(nonComplianceRequest)
                }
            }
        }
    }

    @Test
    fun `Sending non compliance report maps errors from getting the activity`() = runTest {
        coEvery {
            mockCredentialActivityRepository.getById(ACTIVITY_ID)
        } returns Err(ActivityListError.Unexpected(IllegalStateException("error in activity repo")))

        useCase(ACTIVITY_ID, reason, DESCRIPTION, CONTEXT, EMAIL)
            .assertErrorType(NonComplianceError.Unexpected::class)
    }

    @Test
    fun `Sending non compliance report maps errors from getting the credential`() = runTest {
        coEvery {
            mockVerifiableCredentialRepository.getById(CREDENTIAL_ID)
        } returns Err(SsiError.Unexpected(IllegalStateException("error in credential repo")))

        useCase(ACTIVITY_ID, reason, DESCRIPTION, CONTEXT, EMAIL)
            .assertErrorType(NonComplianceError.Unexpected::class)
    }

    @Test
    fun `Sending non compliance report where nonComplianceData is not a presentation request returns an error`() = runTest {
        every { mockCredentialActivity.nonComplianceData } returns "notAPresentationRequest"

        useCase(ACTIVITY_ID, reason, DESCRIPTION, CONTEXT, EMAIL)
            .assertErrorType(NonComplianceError.Unexpected::class)
    }

    @Test
    fun `Sending non compliance report where nonComplianceData is null returns an error`() = runTest {
        every { mockCredentialActivity.nonComplianceData } returns null

        useCase(ACTIVITY_ID, reason, DESCRIPTION, CONTEXT, EMAIL)
            .assertErrorType(NonComplianceError.Unexpected::class)
    }

    @Test
    fun `Sending non compliance report where submitting the report fails returns error`() = runTest {
        coEvery {
            mockSubmitNonComplianceReport(any())
        } returns Err(NonComplianceError.Unexpected(IllegalStateException()))

        useCase(ACTIVITY_ID, reason, DESCRIPTION, CONTEXT, EMAIL)
            .assertErrorType(NonComplianceError.Unexpected::class)
    }

    @Test
    fun `Sending non compliance report for verifier with null activityId returns an error`() = runTest {
        useCase(null, reason, DESCRIPTION, CONTEXT, EMAIL)
            .assertErrorType(NonComplianceError.Unexpected::class)
    }

    @Test
    fun `getPresentationRequestFields uses dcqlQuery when present`() = runTest {
        every { mockCredentialActivity.nonComplianceData } returns "{}"
        val expectedFields = listOf(
            NonComplianceRequestField(name = "vct", constraint = "TestVct"),
            NonComplianceRequestField(name = "$.given_name", constraint = "John"),
        )

        every {
            useCase invoke "getAuthorizationRequest" withArguments listOf(anyNullable<String>())
        } returns Ok(mockAuthorizationRequest)

        every {
            useCase invoke "getPresentationRequestFields" withArguments listOf(mockAuthorizationRequest)
        } returns expectedFields

        val capturedRequest: CapturingSlot<NonComplianceRequest> = slot()
        coEvery {
            mockSubmitNonComplianceReport(capture(capturedRequest))
        } returns Ok(Unit)

        useCase(
            ACTIVITY_ID,
            reason,
            DESCRIPTION,
            CONTEXT,
            EMAIL,
        ).assertOk()

        val metadataFields = capturedRequest.captured.metadata.verifier?.presentationRequestFields
        assertEquals(expectedFields, metadataFields)
    }

    @Test
    fun `getPresentationRequestFields uses presentationDefinition when dcqlQuery is null`() = runTest {
        every { mockCredentialActivity.nonComplianceData } returns "{}"

        val expectedFields = listOf(
            NonComplianceRequestField(name = "path1", constraint = null),
            NonComplianceRequestField(name = "path2", constraint = "constraint2"),
        )

        every {
            useCase invoke "getAuthorizationRequest" withArguments listOf(anyNullable<String>())
        } returns Ok(mockAuthorizationRequest)

        every {
            useCase invoke "getPresentationRequestFields" withArguments listOf(mockAuthorizationRequest)
        } returns expectedFields

        val capturedRequest: CapturingSlot<NonComplianceRequest> = slot()
        coEvery {
            mockSubmitNonComplianceReport(capture(capturedRequest))
        } returns Ok(Unit)

        useCase(
            ACTIVITY_ID,
            reason,
            DESCRIPTION,
            CONTEXT,
            EMAIL,
        ).assertOk()

        val metadataFields = capturedRequest.captured.metadata.verifier?.presentationRequestFields
        assertEquals(expectedFields, metadataFields)
    }

    private fun setupDefaultMocks() {
        every { mockGetCurrentAppLocale() } returns Locale.ENGLISH

        every { mockCredentialActivity.credentialId } returns CREDENTIAL_ID
        every { mockCredentialActivity.createdAt } returns ACTIVITY_CREATED_AT
        every { mockCredentialActivity.nonComplianceData } returns PRESENTATION_REQUEST_JWT

        every { mockClientIdentifier.clientId } returns VERIFIER_DID

        every { mockAuthorizationRequest.clientIdentifier } returns mockClientIdentifier
        every { mockAuthorizationRequest.responseUri } returns VERIFIER_URL
        every { mockAuthorizationRequest.dcqlQuery } returns mockk()

        every { mockVerifiableCredential.issuer } returns ISSUER_DID

        coEvery { mockCredentialActivityRepository.getById(ACTIVITY_ID) } returns Ok(mockCredentialActivity)

        coEvery { mockVerifiableCredentialRepository.getById(CREDENTIAL_ID) } returns Ok(mockVerifiableCredential)

        coEvery { mockSubmitNonComplianceReport(any()) } returns Ok(Unit)
    }

    private fun createNonComplianceRequest(
        nonComplianceData: String = PRESENTATION_REQUEST_JWT,
        language: String = "en"
    ): Pair<NonComplianceRequest, Any?> {
        val metadata = buildMetadata(nonComplianceData)
        val nonComplianceRequest = NonComplianceRequest(
            type = reason.type,
            description = DESCRIPTION,
            context = CONTEXT,
            email = EMAIL,
            language = language,
            metadata = metadata
        )
        return nonComplianceRequest to null
    }

    private fun buildMetadata(nonComplianceData: String): NonComplianceMetadata {
        return NonComplianceMetadata(
            verifier = NonComplianceVerifierMetadata(
                verifierDid = VERIFIER_DID,
                verifierUrl = VERIFIER_URL,
                presentationActionCreatedAt = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(ACTIVITY_CREATED_AT)),
                presentedCredentialIssuerDid = ISSUER_DID,
                presentationRequestJwt = nonComplianceData,
                presentationRequestFields = emptyList()
            )
        )
    }

    private fun inputStrings() = listOf(
        PRESENTATION_REQUEST_JWT,
        presentationRequestJson,
    )

    private companion object {
        const val ACTIVITY_ID = 1L
        const val ACTIVITY_CREATED_AT = 1L
        const val CREDENTIAL_ID = 1L
        val reason = NonComplianceVerifierReportReason.EXCESSIVE_DATA_REQUEST
        const val DESCRIPTION = "description"
        const val CONTEXT = "context"
        const val EMAIL = "email"
        const val VERIFIER_DID = "did:example:12345"
        const val VERIFIER_URL = "https://example.com"
        const val ISSUER_DID = "issuerDid"
        val presentationRequestFields = emptyList<Pair<String, String?>>()
    }
}
