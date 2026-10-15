package ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityListError
import ch.admin.foitt.wallet.platform.activityList.domain.repository.CredentialActivityRepository
import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemEntity
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialActivityEntity
import ch.admin.foitt.wallet.platform.database.domain.model.RawCredentialData
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceIssuerReportReason
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceRequest
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.SendIssuerNonComplianceReport
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialRepo
import ch.admin.foitt.wallet.platform.ssi.domain.repository.RawCredentialDataRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialRepository
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.CapturingSlot
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

class SendIssuerNonComplianceReportImplTest {

    @MockK
    private lateinit var mockCredentialActivityRepository: CredentialActivityRepository

    @MockK
    private lateinit var mockVerifiableCredentialRepository: VerifiableCredentialRepository

    @MockK
    private lateinit var mockCredentialRepo: CredentialRepo

    @MockK
    private lateinit var mockRawCredentialDataRepo: RawCredentialDataRepository

    @MockK
    private lateinit var mockBundleItemRepo: BundleItemRepository

    @MockK
    private lateinit var mockGetCurrentAppLocale: GetCurrentAppLocale

    @MockK
    private lateinit var mockSubmitNonComplianceReport: SubmitNonComplianceReportImpl

    @MockK
    private lateinit var mockCredentialActivity: CredentialActivityEntity

    @MockK
    private lateinit var mockVerifiableCredential: VerifiableCredentialEntity

    @MockK
    private lateinit var mockCredential: Credential

    @MockK
    private lateinit var mockRawCredentialData: RawCredentialData

    private lateinit var useCase: SendIssuerNonComplianceReport

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = SendIssuerNonComplianceReportImpl(
            credentialActivityRepository = mockCredentialActivityRepository,
            verifiableCredentialRepo = mockVerifiableCredentialRepository,
            credentialRepo = mockCredentialRepo,
            rawCredentialDataRepo = mockRawCredentialDataRepo,
            bundleItemRepo = mockBundleItemRepo,
            getCurrentAppLocale = mockGetCurrentAppLocale,
            submitNonComplianceReport = mockSubmitNonComplianceReport
        )

        setupDefaultMocks()
    }

    @Test
    fun `Sending non compliance report for issuer returns Ok`() = runTest {
        val capturedRequest: CapturingSlot<NonComplianceRequest> = slot()
        coEvery { mockSubmitNonComplianceReport(capture(capturedRequest)) } returns Ok(Unit)

        useCase(ACTIVITY_ID, reason, DESCRIPTION, CONTEXT, EMAIL).assertOk()

        coVerify {
            mockCredentialActivityRepository.getById(ACTIVITY_ID)
            mockVerifiableCredentialRepository.getById(CREDENTIAL_ID)
            mockCredentialRepo.getById(CREDENTIAL_ID)
            mockRawCredentialDataRepo.getByCredentialId(CREDENTIAL_ID)
            mockSubmitNonComplianceReport(any())
        }
        coVerify(exactly = 0) { mockBundleItemRepo.getAllByCredentialId(any()) }

        val request = capturedRequest.captured
        assertEquals(reason.type, request.type)
        assertEquals(DESCRIPTION, request.description)
        assertEquals(CONTEXT, request.context)
        assertEquals(EMAIL, request.email)
        assertEquals("en", request.language)

        val metadata = request.metadata.issuer
        assertEquals(ISSUER_DID, metadata?.issuerDid)
        assertEquals(ISSUER_URL.toString(), metadata?.issuerUrl)
        assertEquals(DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(ACTIVITY_CREATED_AT)), metadata?.issuanceCreatedAt)
        assertEquals(SIGNED_METADATA, metadata?.signedMetadataJwt)
        assertEquals(OCA_BUNDLE, metadata?.ocaBundle)
        assertNull(metadata?.vcIssued)
    }

    @Test
    fun `Sending non compliance report for illegal content includes raw VC`() = runTest {
        val illegalReason = NonComplianceIssuerReportReason.ILLEGAL_CONTENT_OR_PURPOSE
        val mockBundleItem = mockk<BundleItemEntity> {
            every { payload } returns RAW_VC
        }
        coEvery { mockBundleItemRepo.getAllByCredentialId(CREDENTIAL_ID) } returns Ok(listOf(mockBundleItem))

        val capturedRequest: CapturingSlot<NonComplianceRequest> = slot()
        coEvery { mockSubmitNonComplianceReport(capture(capturedRequest)) } returns Ok(Unit)

        useCase(ACTIVITY_ID, illegalReason, DESCRIPTION, CONTEXT, EMAIL).assertOk()

        assertEquals(RAW_VC, capturedRequest.captured.metadata.issuer?.vcIssued)
        coVerify { mockBundleItemRepo.getAllByCredentialId(CREDENTIAL_ID) }
    }

    @Test
    fun `Sending non compliance report for illegal content with no VC doesn't include raw VC`() = runTest {
        val illegalReason = NonComplianceIssuerReportReason.ILLEGAL_CONTENT_OR_PURPOSE
        coEvery { mockBundleItemRepo.getAllByCredentialId(CREDENTIAL_ID) } returns Ok(emptyList())

        val capturedRequest: CapturingSlot<NonComplianceRequest> = slot()
        coEvery { mockSubmitNonComplianceReport(capture(capturedRequest)) } returns Ok(Unit)

        useCase(ACTIVITY_ID, illegalReason, DESCRIPTION, CONTEXT, EMAIL).assertOk()

        assertNull(capturedRequest.captured.metadata.issuer?.vcIssued)
        coVerify { mockBundleItemRepo.getAllByCredentialId(CREDENTIAL_ID) }
    }

    @Test
    fun `Sending non compliance report maps errors from getting the bundle items`() = runTest {
        val illegalReason = NonComplianceIssuerReportReason.ILLEGAL_CONTENT_OR_PURPOSE
        coEvery {
            mockBundleItemRepo.getAllByCredentialId(CREDENTIAL_ID)
        } returns Err(SsiError.Unexpected(IllegalStateException("error")))

        useCase(ACTIVITY_ID, illegalReason, DESCRIPTION, CONTEXT, EMAIL)
            .assertErrorType(NonComplianceError.Unexpected::class)
    }

    @Test
    fun `Sending non compliance report for issuer with missing optional metadata returns Ok`() = runTest {
        every { mockRawCredentialData.rawOIDMetadata } returns null
        every { mockRawCredentialData.rawOcaBundle } returns null

        val capturedRequest: CapturingSlot<NonComplianceRequest> = slot()
        coEvery { mockSubmitNonComplianceReport(capture(capturedRequest)) } returns Ok(Unit)

        useCase(ACTIVITY_ID, reason, DESCRIPTION, CONTEXT, EMAIL).assertOk()

        val metadata = capturedRequest.captured.metadata.issuer
        assertNull(metadata?.signedMetadataJwt)
        assertNull(metadata?.ocaBundle)
    }

    @Test
    fun `Sending non compliance report maps errors from getting the activity`() = runTest {
        coEvery {
            mockCredentialActivityRepository.getById(ACTIVITY_ID)
        } returns Err(ActivityListError.Unexpected(IllegalStateException("error")))

        useCase(ACTIVITY_ID, reason, DESCRIPTION, CONTEXT, EMAIL)
            .assertErrorType(NonComplianceError.Unexpected::class)
    }

    @Test
    fun `Sending non compliance report maps errors from getting the VC entity`() = runTest {
        coEvery {
            mockVerifiableCredentialRepository.getById(CREDENTIAL_ID)
        } returns Err(SsiError.Unexpected(IllegalStateException("error")))

        useCase(ACTIVITY_ID, reason, DESCRIPTION, CONTEXT, EMAIL)
            .assertErrorType(NonComplianceError.Unexpected::class)
    }

    @Test
    fun `Sending non compliance report for issuer with null activityId returns an error`() = runTest {
        useCase(null, reason, DESCRIPTION, CONTEXT, EMAIL)
            .assertErrorType(NonComplianceError.Unexpected::class)
    }

    private fun setupDefaultMocks() {
        every { mockGetCurrentAppLocale() } returns Locale.ENGLISH

        every { mockCredentialActivity.credentialId } returns CREDENTIAL_ID
        every { mockCredentialActivity.createdAt } returns ACTIVITY_CREATED_AT

        every { mockVerifiableCredential.issuer } returns ISSUER_DID

        every { mockCredential.issuerUrl } returns ISSUER_URL

        every { mockRawCredentialData.rawOIDMetadata } returns SIGNED_METADATA.toByteArray()
        every { mockRawCredentialData.rawOcaBundle } returns OCA_BUNDLE.toByteArray()

        coEvery { mockCredentialActivityRepository.getById(ACTIVITY_ID) } returns Ok(mockCredentialActivity)
        coEvery { mockVerifiableCredentialRepository.getById(CREDENTIAL_ID) } returns Ok(mockVerifiableCredential)
        coEvery { mockCredentialRepo.getById(CREDENTIAL_ID) } returns Ok(mockCredential)
        coEvery { mockRawCredentialDataRepo.getByCredentialId(CREDENTIAL_ID) } returns Ok(mockRawCredentialData)
        coEvery { mockSubmitNonComplianceReport(any()) } returns Ok(Unit)
    }

    private companion object {
        const val ACTIVITY_ID = 1L
        const val ACTIVITY_CREATED_AT = 1L
        const val CREDENTIAL_ID = 1L
        val reason = NonComplianceIssuerReportReason.DUBIOUS_CREDENTIAL_ISSUANCE
        const val DESCRIPTION = "description"
        const val CONTEXT = "context"
        const val EMAIL = "email"
        const val ISSUER_DID = "issuerDid"
        val ISSUER_URL = URL("https://example.com")
        const val SIGNED_METADATA = "signedMetadata"
        const val OCA_BUNDLE = "ocaBundle"
        const val RAW_VC = "rawVc"
    }
}
