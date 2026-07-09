package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.TokenType
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialAuthenticationEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialAuthenticationWithDpopBinding
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialKeyBindingEntity
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialWithAuthenticationAndKeyBinding
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialRepo
import ch.admin.foitt.wallet.platform.ssi.domain.repository.DeferredCredentialRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteDeferred
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteKeyStoreEntry
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeleteDeferredImplTest {

    @MockK
    private lateinit var mockCredentialRepository: CredentialRepo

    @MockK
    private lateinit var mockDeferredCredentialRepository: DeferredCredentialRepository

    @MockK
    private lateinit var mockCredential: Credential

    @MockK
    private lateinit var mockDeferredCredential: DeferredCredentialEntity

    @MockK
    private lateinit var mockKeyBinding: CredentialKeyBindingEntity

    @MockK
    private lateinit var deleteKeyStoreEntry: DeleteKeyStoreEntry

    private lateinit var useCase: DeleteDeferred

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { mockCredential.id } returns CREDENTIAL_ID
        every { mockKeyBinding.id } returns PRIVATE_KEY_IDENTIFIER

        val deferredCredentialWithAuthenticationAndKeyBinding = DeferredCredentialWithAuthenticationAndKeyBinding(
            credential = mockCredential,
            keyBindings = listOf(mockKeyBinding),
            deferredCredential = mockDeferredCredential,
            authentication = CredentialAuthenticationWithDpopBinding(
                credentialAuthentication = CredentialAuthenticationEntity(
                    credentialId = mockCredential.id,
                    tokenType = TokenType.BEARER,
                    accessToken = ACCESS_TOKEN,
                    refreshToken = REFRESH_TOKEN,
                ),
                dpopBinding = null,
            )
        )
        coEvery {
            mockDeferredCredentialRepository.getById(
                CREDENTIAL_ID
            )
        } returns Ok(deferredCredentialWithAuthenticationAndKeyBinding)
        coEvery { mockCredentialRepository.deleteById(CREDENTIAL_ID) } returns Ok(Unit)

        coEvery { deleteKeyStoreEntry(PRIVATE_KEY_IDENTIFIER) } returns Unit

        useCase = DeleteDeferredImpl(
            credentialRepository = mockCredentialRepository,
            deleteKeyStoreEntry = deleteKeyStoreEntry,
            deferredCredentialRepository = mockDeferredCredentialRepository,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Deleting a deferred credential succeeds and runs specific steps`() = runTest {
        useCase(CREDENTIAL_ID).assertOk()

        coVerify(exactly = 1) {
            mockDeferredCredentialRepository.getById(CREDENTIAL_ID)
            mockCredentialRepository.deleteById(CREDENTIAL_ID)
        }
    }

    @Test
    fun `Deleting a deferred credential maps errors from getting the credential`() = runTest {
        coEvery {
            mockDeferredCredentialRepository.getById(CREDENTIAL_ID)
        } returns Err(SsiError.Unexpected(Exception()))

        useCase(CREDENTIAL_ID).assertErrorType(SsiError.Unexpected::class)
    }

    @Test
    fun `Deleting a deferred credential maps errors from deleting the credential`() = runTest {
        coEvery { mockCredentialRepository.deleteById(CREDENTIAL_ID) } returns Err(SsiError.Unexpected(Exception()))

        useCase(CREDENTIAL_ID).assertErrorType(SsiError.Unexpected::class)
    }

    private companion object {
        const val CREDENTIAL_ID = 1L
        const val ACCESS_TOKEN = "access-token"
        const val REFRESH_TOKEN = "refresh-token"
        const val PRIVATE_KEY_IDENTIFIER = "privateKeyIdentifier"
    }
}
