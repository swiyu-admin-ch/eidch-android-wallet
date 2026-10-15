package ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceIssuerReportReason
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceVerifierReportReason
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.SendIssuerNonComplianceReport
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.SendNonComplianceReport
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.SendVerifierNonComplianceReport
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SendNonComplianceReportImplTest {

    @MockK
    private lateinit var mockSendIssuerReport: SendIssuerNonComplianceReport

    @MockK
    private lateinit var mockSendVerifierReport: SendVerifierNonComplianceReport

    private lateinit var useCase: SendNonComplianceReport

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = SendNonComplianceReportImpl(
            sendIssuerReport = mockSendIssuerReport,
            sendVerifierReport = mockSendVerifierReport,
        )
    }

    @Test
    fun `Sending non compliance report for verifier calls verifier use case`() = runTest {
        val reason = NonComplianceVerifierReportReason.EXCESSIVE_DATA_REQUEST
        coEvery {
            mockSendVerifierReport(ACTIVITY_ID, reason, DESCRIPTION, CONTEXT, EMAIL)
        } returns Ok(Unit)

        useCase(ACTIVITY_ID, reason, DESCRIPTION, CONTEXT, EMAIL).assertOk()

        coVerify {
            mockSendVerifierReport(ACTIVITY_ID, reason, DESCRIPTION, CONTEXT, EMAIL)
        }
        coVerify(exactly = 0) {
            mockSendIssuerReport(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `Sending non compliance report for issuer calls issuer use case`() = runTest {
        val reason = NonComplianceIssuerReportReason.DUBIOUS_CREDENTIAL_ISSUANCE
        coEvery {
            mockSendIssuerReport(ACTIVITY_ID, reason, DESCRIPTION, CONTEXT, EMAIL)
        } returns Ok(Unit)

        useCase(ACTIVITY_ID, reason, DESCRIPTION, CONTEXT, EMAIL).assertOk()

        coVerify {
            mockSendIssuerReport(ACTIVITY_ID, reason, DESCRIPTION, CONTEXT, EMAIL)
        }
        coVerify(exactly = 0) {
            mockSendVerifierReport(any(), any(), any(), any(), any())
        }
    }

    private companion object {
        const val ACTIVITY_ID = 1L
        const val DESCRIPTION = "description"
        const val CONTEXT = "context"
        const val EMAIL = "email"
    }
}
