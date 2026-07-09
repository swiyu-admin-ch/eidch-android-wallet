package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.BatchSize
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.BatchCredentialIssuance
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.EvaluateBatchSize
import ch.admin.foitt.wallet.util.assertErr
import ch.admin.foitt.wallet.util.assertOk
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class EvaluateBatchSizeImplTest {

    private val useCase: EvaluateBatchSize = EvaluateBatchSizeImpl()

    @Test
    fun `Returns an error when batch issuance info is missing`() {
        val issuerCredentialInfo = mockk<IssuerCredentialInfo> {
            every { batchCredentialIssuance } returns null
        }

        val error = useCase(issuerCredentialInfo).assertErr()

        assertEquals(CredentialError.InvalidIssuerCredentialInfo, error)
    }

    @ParameterizedTest
    @ValueSource(ints = [-1, 0, 9, 101])
    fun `Returns an error when batch size is out of allowed bounds`(batchSize: Int) {
        val issuerCredentialInfo = createIssuerCredentialInfo(batchSize = batchSize)

        val error = useCase(issuerCredentialInfo).assertErr()

        assertEquals(CredentialError.InvalidIssuerCredentialInfo, error)
    }

    @ParameterizedTest
    @ValueSource(ints = [11, 50, 99])
    fun `Returns batch size when it is within allowed bounds`(batchSize: Int) {
        val issuerCredentialInfo = createIssuerCredentialInfo(batchSize = batchSize)

        val result = useCase(issuerCredentialInfo).assertOk()

        assertEquals(batchSize, result)
    }

    private fun createIssuerCredentialInfo(batchSize: BatchSize) = mockk<IssuerCredentialInfo> {
        every { batchCredentialIssuance } returns BatchCredentialIssuance(batchSize = batchSize)
    }
}
