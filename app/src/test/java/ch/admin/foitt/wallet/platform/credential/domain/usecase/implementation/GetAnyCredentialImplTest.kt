package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.GetAllAnyCredentialsByCredentialIdError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAllAnyCredentialsByCredentialId
import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemEntity
import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemWithKeyBinding
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialKeyBindingEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithBundleItemsWithKeyBinding
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithBundleItemsWithKeyBindingRepository
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class GetAnyCredentialImplTest {

    @MockK
    private lateinit var mockCredentialWithKeyBindingRepository:
        VerifiableCredentialWithBundleItemsWithKeyBindingRepository

    private lateinit var useCase: GetAllAnyCredentialsByCredentialId

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = GetAllAnyCredentialsByCredentialIdImpl(mockCredentialWithKeyBindingRepository)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting any credential matching vc+sd_jwt credential returns it`() = runTest {
        val mockCredential = createMockCredentialWithKeyBinding()
        coEvery { mockCredentialWithKeyBindingRepository.getByCredentialId(CREDENTIAL_ID) } returns Ok(mockCredential)

        val anyCredential = useCase(CREDENTIAL_ID).assertOk().first()

        assertTrue(anyCredential is VcSdJwtCredential)
        assertEquals(CREDENTIAL_ID, anyCredential.id)
        assertEquals(PAYLOAD, anyCredential.payload)
        assertNotNull(anyCredential.keyBinding)
        assertEquals(KEY_BINDING_ID, anyCredential.keyBinding?.identifier)
        assertEquals(KEY_BINDING_ALGORITHM, anyCredential.keyBinding?.algorithm)
    }

    @Test
    fun `Getting any credential with none available returns an error`() = runTest {
        val exception = IllegalStateException("no credential found")
        coEvery { mockCredentialWithKeyBindingRepository.getByCredentialId(any()) } returns Err(SsiError.Unexpected(exception))

        useCase(CREDENTIAL_ID).assertErrorType(GetAllAnyCredentialsByCredentialIdError::class)
    }

    @Test
    fun `Getting any credential with other format returns an error`() = runTest {
        val mockCredential = createMockCredentialWithKeyBinding(format = CredentialFormat.UNKNOWN)
        coEvery { mockCredentialWithKeyBindingRepository.getByCredentialId(CREDENTIAL_ID) } returns Ok(mockCredential)

        val result = useCase(CREDENTIAL_ID)

        result.assertErrorType(CredentialError.Unexpected::class)
    }

    @Test
    fun `Getting any credential with unknown signing algorithm returns an error`() = runTest {
        val mockCredential = createMockCredentialWithKeyBinding(keyBindingAlgorithm = "other")
        coEvery { mockCredentialWithKeyBindingRepository.getByCredentialId(CREDENTIAL_ID) } returns Ok(mockCredential)

        val result = useCase(CREDENTIAL_ID)

        result.assertErrorType(CredentialError.Unexpected::class)
    }

    @Test
    fun `Getting any credential maps errors from credential repository`() = runTest {
        val exception = IllegalStateException()
        coEvery { mockCredentialWithKeyBindingRepository.getByCredentialId(any()) } returns Err(SsiError.Unexpected(exception))

        val result = useCase(CREDENTIAL_ID)

        val error = result.assertErrorType(CredentialError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    private fun createMockCredentialWithKeyBinding(
        format: CredentialFormat = CredentialFormat.VC_SD_JWT,
        keyBindingAlgorithm: String = KEY_BINDING_ALGORITHM.stdName,
    ) = VerifiableCredentialWithBundleItemsWithKeyBinding(
        credential = createMockCredential(format),
        verifiableCredential = createMockVerifiableCredential(),
        bundleItemsWithKeyBinding = listOf(
            BundleItemWithKeyBinding(
                bundleItem = createMockBundleItem(),
                keyBinding = createMockKeyBinding(keyBindingAlgorithm),
            )
        ),
    )

    private fun createMockBundleItem() = BundleItemEntity(
        id = BUNDLE_ITEM_ID,
        credentialId = CREDENTIAL_ID,
        payload = PAYLOAD
    )

    private fun createMockCredential(
        format: CredentialFormat = CredentialFormat.VC_SD_JWT,
    ) = Credential(
        id = CREDENTIAL_ID,
        format = format,
        issuerUrl = URL("https://example.com/issuer")
    )

    private fun createMockVerifiableCredential() = VerifiableCredentialEntity(
        issuer = "issuer",
        validFrom = 0,
        validUntil = 17768026519L,
        credentialId = CREDENTIAL_ID,
        nextPresentableBundleItemId = 1,
    )

    private fun createMockKeyBinding(
        keyBindingAlgorithm: String = KEY_BINDING_ALGORITHM.stdName,
    ) = CredentialKeyBindingEntity(
        id = KEY_BINDING_ID,
        credentialId = CREDENTIAL_ID,
        algorithm = keyBindingAlgorithm,
        bindingType = KeyBindingType.HARDWARE,
    )

    private companion object {
        const val CREDENTIAL_ID = 1L
        const val BUNDLE_ITEM_ID = 1L
        const val KEY_BINDING_ID = "privateKeyIdentifier"
        const val PAYLOAD =
            "ewogICJ0eXAiOiJ2YytzZC1qd3QiLAogICJhbGciOiJFUzI1NiIsCiAgImtpZCI6ImtleUlkIgp9.ewogICJpc3MiOiJkaWQ6dGR3OmlkZW50aWZpZXIiLAogICJ2Y3QiOiJ2Y3QiCn0.ZXdvZ0lDSjBlWEFpT2lKMll5dHpaQzFxZDNRaUxBb2dJQ0poYkdjaU9pSkZVekkxTmlJc0NpQWdJbXRwWkNJNkltdGxlVWxrSWdwOS4uNHNwTXBzWE1nYlNyY0lqMFdNbXJNYXdhcVRzeG9GWmItcjdwTWlubEhvZklRRUhhS2pzV1J0dENzUTkyd0tfa3RpaDQta2VCdjdVbkc2MkRPa2NDbGc~"
        val KEY_BINDING_ALGORITHM = SigningAlgorithm.ES512
    }
}
