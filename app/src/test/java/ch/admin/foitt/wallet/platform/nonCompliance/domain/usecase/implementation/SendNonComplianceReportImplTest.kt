package ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityListError
import ch.admin.foitt.wallet.platform.activityList.domain.repository.CredentialActivityRepository
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationPoP
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.GenerateProofOfPossession
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.RequestClientAttestation
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialActivityEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceChallengeResponse
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceMetadata
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceReportReason
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceRequest
import ch.admin.foitt.wallet.platform.nonCompliance.domain.repository.NonComplianceRepository
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.SendNonComplianceReport
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
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.time.Instant
import java.time.format.DateTimeFormatter

@OptIn(UnsafeResultValueAccess::class)
class SendNonComplianceReportImplTest {

    @MockK
    private lateinit var mockCredentialActivityRepository: CredentialActivityRepository

    @MockK
    private lateinit var mockVerifiableCredentialRepository: VerifiableCredentialRepository

    @MockK
    private lateinit var mockRequestClientAttestation: RequestClientAttestation

    @MockK
    private lateinit var mockNonComplianceRepository: NonComplianceRepository

    @MockK
    private lateinit var mockGenerateProofOfPossession: GenerateProofOfPossession

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    @MockK
    private lateinit var mockCredentialActivity: CredentialActivityEntity

    @MockK
    private lateinit var mockVerifiableCredential: VerifiableCredentialEntity

    @MockK
    private lateinit var mockClientAttestation: ClientAttestation

    @MockK
    private lateinit var mockClientAttestationPoP: ClientAttestationPoP

    @MockK
    private lateinit var mockNonComplianceChallengeResponse: NonComplianceChallengeResponse

    private lateinit var useCase: SendNonComplianceReport

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = SendNonComplianceReportImpl(
            credentialActivityRepository = mockCredentialActivityRepository,
            verifiableCredentialRepo = mockVerifiableCredentialRepository,
            requestClientAttestation = mockRequestClientAttestation,
            nonComplianceRepository = mockNonComplianceRepository,
            generateProofOfPossession = mockGenerateProofOfPossession,
            environmentSetupRepository = mockEnvironmentSetupRepository,
            safeJson = safeJson,
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

                val (nonComplianceRequest, requestBody) = createNonComplianceRequest(input)

                coEvery {
                    mockGenerateProofOfPossession(
                        clientAttestation = mockClientAttestation,
                        challenge = POP_CHALLENGE,
                        audience = NON_COMPLIANCE_BASE_URL,
                        requestBody = requestBody,
                    )
                } returns Ok(mockClientAttestationPoP)

                coEvery {
                    mockNonComplianceRepository.sendReport(
                        mockClientAttestation,
                        mockClientAttestationPoP,
                        nonComplianceRequest,
                    )
                } returns Ok(Unit)

                useCase(ACTIVITY_ID, reason, DESCRIPTION, EMAIL).assertOk()

                coVerify {
                    mockCredentialActivityRepository.getById(ACTIVITY_ID)
                    mockVerifiableCredentialRepository.getById(CREDENTIAL_ID)
                    mockRequestClientAttestation()
                    mockNonComplianceRepository.fetchChallenge()
                    safeJson.safeEncodeObjectToJsonElement(nonComplianceRequest)
                    mockGenerateProofOfPossession(mockClientAttestation, POP_CHALLENGE, NON_COMPLIANCE_BASE_URL, requestBody)
                    mockNonComplianceRepository.sendReport(mockClientAttestation, mockClientAttestationPoP, nonComplianceRequest)
                }
            }
        }
    }

    @Test
    fun `Sending non compliance report maps errors from getting the activity`() = runTest {
        coEvery {
            mockCredentialActivityRepository.getById(ACTIVITY_ID)
        } returns Err(ActivityListError.Unexpected(IllegalStateException("error in activity repo")))

        useCase(ACTIVITY_ID, reason, DESCRIPTION, EMAIL)
            .assertErrorType(NonComplianceError.Unexpected::class)
    }

    @Test
    fun `Sending non compliance report maps errors from getting the credential`() = runTest {
        coEvery {
            mockVerifiableCredentialRepository.getById(CREDENTIAL_ID)
        } returns Err(SsiError.Unexpected(IllegalStateException("error in credential repo")))

        useCase(ACTIVITY_ID, reason, DESCRIPTION, EMAIL)
            .assertErrorType(NonComplianceError.Unexpected::class)
    }

    @Test
    fun `Sending non compliance report where nonComplianceData is not a presentation request returns an error`() = runTest {
        every { mockCredentialActivity.nonComplianceData } returns "notAPresentationRequest"

        useCase(ACTIVITY_ID, reason, DESCRIPTION, EMAIL)
            .assertErrorType(NonComplianceError.Unexpected::class)
    }

    @Test
    fun `Sending non compliance report where client attestation fails returns error`() = runTest {
        coEvery { mockRequestClientAttestation() } returns Err(AttestationError.ValidationError("message"))

        useCase(ACTIVITY_ID, reason, DESCRIPTION, EMAIL)
            .assertErrorType(NonComplianceError.InvalidClientAttestation::class)
    }

    @Test
    fun `Sending non compliance report where fetching the proof of possession challenge fails returns error`() = runTest {
        coEvery {
            mockNonComplianceRepository.fetchChallenge()
        } returns Err(NonComplianceError.Unexpected(IllegalStateException("exception")))

        useCase(ACTIVITY_ID, reason, DESCRIPTION, EMAIL)
            .assertErrorType(NonComplianceError.Unexpected::class)
    }

    @Test
    fun `Sending non compliance report where generating the proof of possession fails returns error`() = runTest {
        coEvery {
            mockGenerateProofOfPossession(any(), any(), any(), any())
        } returns Err(AttestationError.Unexpected(IllegalStateException()))

        useCase(ACTIVITY_ID, reason, DESCRIPTION, EMAIL)
            .assertErrorType(NonComplianceError.Unexpected::class)
    }

    @Test
    fun `Sending non compliance report where sending the report fails returns error`() = runTest {
        coEvery {
            mockNonComplianceRepository.sendReport(any(), any(), any())
        } returns Err(NonComplianceError.Unexpected(IllegalStateException()))

        useCase(ACTIVITY_ID, reason, DESCRIPTION, EMAIL)
            .assertErrorType(NonComplianceError.Unexpected::class)
    }

    private fun setupDefaultMocks() {
        every { mockCredentialActivity.credentialId } returns CREDENTIAL_ID
        every { mockCredentialActivity.createdAt } returns ACTIVITY_CREATED_AT
        every { mockCredentialActivity.nonComplianceData } returns PRESENTATION_REQUEST_JWT

        every { mockVerifiableCredential.issuer } returns ISSUER_DID

        coEvery { mockCredentialActivityRepository.getById(ACTIVITY_ID) } returns Ok(mockCredentialActivity)

        coEvery { mockVerifiableCredentialRepository.getById(CREDENTIAL_ID) } returns Ok(mockVerifiableCredential)

        every { mockNonComplianceChallengeResponse.challenge } returns POP_CHALLENGE

        coEvery { mockRequestClientAttestation() } returns Ok(mockClientAttestation)

        coEvery { mockNonComplianceRepository.fetchChallenge() } returns Ok(mockNonComplianceChallengeResponse)

        coEvery { mockEnvironmentSetupRepository.nonComplianceBaseUrl } returns NON_COMPLIANCE_BASE_URL

        val (nonComplianceRequest, requestBody) = createNonComplianceRequest()

        coEvery {
            mockGenerateProofOfPossession(
                clientAttestation = mockClientAttestation,
                challenge = POP_CHALLENGE,
                audience = NON_COMPLIANCE_BASE_URL,
                requestBody = requestBody,
            )
        } returns Ok(mockClientAttestationPoP)

        coEvery {
            mockNonComplianceRepository.sendReport(
                mockClientAttestation,
                mockClientAttestationPoP,
                nonComplianceRequest,
            )
        } returns Ok(Unit)
    }

    private fun createNonComplianceRequest(nonComplianceData: String = PRESENTATION_REQUEST_JWT): Pair<NonComplianceRequest, JsonElement> {
        val metadata = buildMetadata(nonComplianceData)
        val nonComplianceRequest = NonComplianceRequest(
            type = reason.type,
            description = DESCRIPTION,
            email = EMAIL,
            metadata = metadata
        )
        val requestBody = safeJson.safeEncodeObjectToJsonElement(nonComplianceRequest).value

        return nonComplianceRequest to requestBody
    }

    private fun buildMetadata(nonComplianceData: String): NonComplianceMetadata {
        val metadataJson = buildJsonObject {
            put("verifier_did", VERIFIER_DID)
            put("verifier_url", VERIFIER_URL)
            put("presentation_action_created_at", DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(ACTIVITY_CREATED_AT)))
            put("presented_credential_issuer_did", ISSUER_DID)
            put("presentation_request_jwt", nonComplianceData)
            putJsonArray("presentation_request_fields") {
                presentationRequestFields.let { fields ->
                    fields.forEach { field ->
                        add(
                            buildJsonObject {
                                put("name", field.first)
                                field.second?.let { put("constraint", it) }
                            }
                        )
                    }
                }
            }
        }
        val metadataString = metadataJson.toString()
        val metadata = safeJson.safeDecodeStringTo<NonComplianceMetadata>(metadataString).value
        return metadata
    }

    private fun inputStrings() = listOf(
        PRESENTATION_REQUEST_JWT,
        presentationRequestJson,
    )

    private companion object {
        const val ACTIVITY_ID = 1L
        const val ACTIVITY_CREATED_AT = 1L
        const val CREDENTIAL_ID = 1L
        const val NON_COMPLIANCE_BASE_URL = "nonComplianceBaseUrl"
        const val POP_CHALLENGE = "challenge"
        val reason = NonComplianceReportReason.EXCESSIVE_DATA_REQUEST
        const val DESCRIPTION = "description"
        const val EMAIL = "email"
        const val VERIFIER_DID = "did:example:12345"
        const val VERIFIER_URL = "https://example.com"
        const val ISSUER_DID = "issuerDid"
        val presentationRequestFields = listOf(("path1" to null), ("path2" to "constraint2"))
    }
}
