package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetCredentialConfigImplTest {

    @MockK
    private lateinit var mockConfig1: AnyCredentialConfiguration

    @MockK
    private lateinit var mockConfig2: AnyCredentialConfiguration

    private lateinit var useCase: GetCredentialConfigImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { mockConfig1.identifier } returns IDENTIFIER_1
        every { mockConfig2.identifier } returns IDENTIFIER_2

        useCase = GetCredentialConfigImpl()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `returns first matching configuration when multiple identifiers match`() = runTest {
        val credentials = listOf(IDENTIFIER_1, IDENTIFIER_2)
        val configurations = listOf(mockConfig1, mockConfig2)

        val result = useCase.invoke(credentials, configurations).assertOk()

        assert(result === mockConfig1)
    }

    @Test
    fun `returns error when no configuration matches provided identifiers`() = runTest {
        val credentials = listOf("unknown-credential")
        val configurations = listOf(mockConfig1, mockConfig2)

        val result = useCase.invoke(credentials, configurations)

        result.assertErrorType(CredentialError.UnsupportedCredentialIdentifier::class)
    }

    private companion object {
        const val IDENTIFIER_1 = "id-1"
        const val IDENTIFIER_2 = "id-2"
    }
}
