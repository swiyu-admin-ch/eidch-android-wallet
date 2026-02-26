package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateAllCredentialStatuses
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateCredentialStatus
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation.mock.MockCredentialsWithBundleItems.verifiableCredentialsWithBundleItems
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialRepository
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateAllCredentialStatusesImplTest {
    @MockK
    private lateinit var mockVerifiableCredentialRepo: VerifiableCredentialRepository

    @MockK
    private lateinit var mockUpdateCredentialStatus: UpdateCredentialStatus

    private lateinit var useCase: UpdateAllCredentialStatuses

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = UpdateAllCredentialStatusesImpl(
            verifiableCredentialRepository = mockVerifiableCredentialRepo,
            updateCredentialStatus = mockUpdateCredentialStatus
        )

        success()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Updating all credential statuses updates all credentials`() = runTest {
        useCase()

        coVerify(exactly = 3) {
            mockUpdateCredentialStatus(any())
        }
    }

    @Test
    fun `Updating all credential statuses when getting credentials fails silently fails`() = runTest {
        coEvery { mockVerifiableCredentialRepo.getAllIds() } returns Err(SsiError.Unexpected(Exception()))

        useCase()

        coVerify(exactly = 0) {
            mockUpdateCredentialStatus(any())
        }
    }

    @Test
    fun `Updating all credential statuses when updating of one credential fails silently fails`() = runTest {
        coEvery { mockUpdateCredentialStatus(verifiableCredentialsWithBundleItems[1].credential.id) } returns Err(
            CredentialStatusError.Unexpected(
                Exception()
            )
        )

        useCase()

        coVerify(exactly = 3) {
            mockUpdateCredentialStatus(any())
        }
    }

    private fun success() {
        coEvery {
            mockVerifiableCredentialRepo.getAllIds()
        } returns Ok(verifiableCredentialsWithBundleItems.map { it.credential.id })
        coEvery {
            mockUpdateCredentialStatus(any())
        } returns Ok(Unit)
    }
}
