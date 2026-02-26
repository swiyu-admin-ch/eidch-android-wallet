package ch.admin.foitt.wallet.feature.eIdRequestVerification

import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.EIdRequestVerificationError
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation.GetEIdRequestCaseImpl
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestCase
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.IdentityType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseRepository
import ch.admin.foitt.wallet.util.assertErrorType
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetEIdRequestCaseImplTest {

    @MockK
    private lateinit var repository: EIdRequestCaseRepository
    private lateinit var useCase: GetEIdRequestCaseImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { repository.getEIdRequestCase(any()) } returns Ok(
            EIdRequestCase(
                id = "id",
                credentialId = 1L,
                rawMrz = "rawMRZ",
                documentNumber = "12K",
                selectedDocumentType = IdentityType.SWISS_PASS,
                firstName = "firstName",
                lastName = "lastName",
                createdAt = 2
            )
        )

        useCase = GetEIdRequestCaseImpl(repository)
    }

    @Test
    fun `invoke should return success when repository returns success`() = runTest {
        val caseId = "test_case_id"
        val mockEIdCase = mockk<EIdRequestCase>()
        coEvery { repository.getEIdRequestCase(caseId) } returns Ok(mockEIdCase)

        val result = useCase(caseId)

        assertEquals(mockEIdCase, result.get())
    }

    @Test
    fun `invoke should return mapped error when repository fails with UnexpectedError`() = runTest {
        val exception = Exception("myError")
        val caseId = "test_case_id"

        coEvery { repository.getEIdRequestCase(caseId) } returns Err(EIdRequestError.Unexpected(exception))

        val result = useCase(caseId)

        val error = result.assertErrorType(EIdRequestVerificationError.Unexpected::class)
        assertEquals(exception, error.cause)
    }
}
