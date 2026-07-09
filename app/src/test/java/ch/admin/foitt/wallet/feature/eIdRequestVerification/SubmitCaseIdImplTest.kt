package ch.admin.foitt.wallet.feature.eIdRequestVerification

import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SubmitCaseId
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation.SubmitCaseIdImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdAvRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseRepository
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SubmitCaseIdImplTest {
    @MockK
    private lateinit var mockEIdAvRepository: EIdAvRepository

    @MockK
    private lateinit var mockEIdRequestCaseRepository: EIdRequestCaseRepository

    lateinit var submitCaseId: SubmitCaseId

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        submitCaseId = SubmitCaseIdImpl(
            eIdAvRepository = mockEIdAvRepository,
            eIdRequestCaseRepository = mockEIdRequestCaseRepository,
        )

        setupDefaultMocks()
    }

    private fun setupDefaultMocks() {
        coEvery {
            mockEIdAvRepository.submitCase(any(), any())
        } returns Ok(Unit)

        coEvery {
            mockEIdRequestCaseRepository.setFilesSubmitted(any())
        } returns Ok(Unit)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Successfully submit a case returns an Ok`() = runTest {
        submitCaseId(caseId = CASE_ID, accessToken = ACCESS_TOKEN).assertOk()

        coVerifyOrder {
            mockEIdAvRepository.submitCase(caseId = CASE_ID, accessToken = ACCESS_TOKEN)
            mockEIdRequestCaseRepository.setFilesSubmitted(caseId = CASE_ID)
        }
    }

    @Test
    fun `Error when submitting a case from the repository is propagated`() = runTest {
        val exception = Exception("error in db")
        coEvery {
            mockEIdAvRepository.submitCase(any(), any())
        } returns Err(EIdRequestError.Unexpected(exception))

        submitCaseId(caseId = CASE_ID, accessToken = ACCESS_TOKEN).assertErrorType(EIdRequestError.Unexpected::class)
    }

    @Test
    fun `Error when setting files submitted is propagated`() = runTest {
        val exception = Exception("error in db")
        coEvery {
            mockEIdRequestCaseRepository.setFilesSubmitted(any(), any())
        } returns Err(EIdRequestError.Unexpected(exception))

        submitCaseId(caseId = CASE_ID, accessToken = ACCESS_TOKEN).assertErrorType(EIdRequestError.Unexpected::class)
    }

    private companion object {
        const val CASE_ID = "caseId"
        const val ACCESS_TOKEN = "accessToken"
    }
}
