package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAllAnyCredentials
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

class GetAnyCredentialsImplTest {

    @MockK
    private lateinit var mockCredentialWithKeyBindingRepository:
        VerifiableCredentialWithBundleItemsWithKeyBindingRepository

    private lateinit var useCase: GetAllAnyCredentials

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = GetAllAnyCredentialsImpl(mockCredentialWithKeyBindingRepository)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting any credentials with none available returns empty list`() = runTest {
        coEvery { mockCredentialWithKeyBindingRepository.getAll() } returns Ok(emptyList())

        val result = useCase().assertOk()

        assertEquals(0, result.size)
    }

    @Test
    fun `Getting any credentials with one vc+sd_jwt available returns it`() = runTest {
        val mockCredential = createMockCredentialWithKeyBinding()
        coEvery { mockCredentialWithKeyBindingRepository.getAll() } returns Ok(listOf(mockCredential))

        val result = useCase().assertOk()

        assertEquals(1, result.size)
        val credential = result.first()
        assertTrue(credential is VcSdJwtCredential)
        assertEquals(CREDENTIAL_ID, credential.id)
        assertEquals(PAYLOAD, credential.payload)

        assertNotNull(credential.keyBinding)
        assertEquals(KEY_BINDING_IDENTIFIER, credential.keyBinding?.identifier)
        assertEquals(KEY_BINDING_ALGORITHM, credential.keyBinding?.algorithm)
    }

    @Test
    fun `Getting any credentials with two vc+sd_jwt available returns both`() = runTest {
        val mockCredential = createMockCredentialWithKeyBinding()
        val mockCredential2 = createMockCredentialWithKeyBinding(
            id = CREDENTIAL_ID_2,
            keyBindingIdentifier = KEY_BINDING_IDENTIFIER_2,
            keyBindingAlgorithm = KEY_BINDING_ALGORITHM_2.stdName,
        )
        coEvery { mockCredentialWithKeyBindingRepository.getAll() } returns Ok(listOf(mockCredential, mockCredential2))

        val result = useCase().assertOk()

        assertEquals(2, result.size)

        val credential1 = result[0]
        assertTrue(credential1 is VcSdJwtCredential)
        assertEquals(CREDENTIAL_ID, credential1.id)
        assertEquals(PAYLOAD, credential1.payload)
        assertNotNull(credential1.keyBinding)
        assertEquals(KEY_BINDING_IDENTIFIER, credential1.keyBinding?.identifier)
        assertEquals(KEY_BINDING_ALGORITHM, credential1.keyBinding?.algorithm)

        val credential2 = result[1]
        assertTrue(credential2 is VcSdJwtCredential)
        assertEquals(CREDENTIAL_ID_2, credential2.id)
        assertEquals(PAYLOAD_2, credential2.payload)
        assertEquals(KEY_BINDING_IDENTIFIER_2, credential2.keyBinding?.identifier)
        assertEquals(KEY_BINDING_ALGORITHM_2, credential2.keyBinding?.algorithm)
    }

    @Test
    fun `Getting any credentials with other format available returns an empty list`() = runTest {
        val mockCredential = createMockCredentialWithKeyBinding(format = CredentialFormat.UNKNOWN)
        coEvery { mockCredentialWithKeyBindingRepository.getAll() } returns Ok(listOf(mockCredential))

        val result = useCase().assertOk()
        assertEquals(emptyList<AnyCredential>(), result)
    }

    @Test
    fun `Getting any credentials with unknown signing algorithm returns an empty list`() = runTest {
        val mockCredential = createMockCredentialWithKeyBinding(keyBindingAlgorithm = "other")

        coEvery { mockCredentialWithKeyBindingRepository.getAll() } returns Ok(listOf(mockCredential))

        val result = useCase().assertOk()
        assertEquals(emptyList<AnyCredential>(), result)
    }

    @Test
    fun `Getting any credentials maps errors from credential repository`() = runTest {
        val exception = IllegalStateException()
        coEvery { mockCredentialWithKeyBindingRepository.getAll() } returns Err(SsiError.Unexpected(exception))

        val result = useCase()

        val error = result.assertErrorType(CredentialError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    private fun createMockCredentialWithKeyBinding(
        id: Long = CREDENTIAL_ID,
        keyBindingIdentifier: String = KEY_BINDING_IDENTIFIER,
        format: CredentialFormat = CredentialFormat.VC_SD_JWT,
        keyBindingAlgorithm: String = KEY_BINDING_ALGORITHM.stdName,
    ) = VerifiableCredentialWithBundleItemsWithKeyBinding(
        credential = createMockCredential(id, format),
        verifiableCredential = createMockVerifiableCredential(id),
        bundleItemsWithKeyBinding = listOf(
            BundleItemWithKeyBinding(
                bundleItem = createMockBundleItem(),
                keyBinding = createMockKeyBinding(keyBindingIdentifier, keyBindingAlgorithm),
            )
        )
    )

    private fun createMockBundleItem() = BundleItemEntity(
        id = BUNDLE_ITEM_ID,
        credentialId = CREDENTIAL_ID,
        payload = PAYLOAD
    )

    private fun createMockCredential(
        id: Long = CREDENTIAL_ID,
        format: CredentialFormat = CredentialFormat.VC_SD_JWT,
    ) = Credential(
        id = id,
        format = format,
        issuerUrl = URL("https://example.com/issuer"),
    )

    private fun createMockVerifiableCredential(
        id: Long = CREDENTIAL_ID,
    ) = VerifiableCredentialEntity(
        issuer = "issuer",
        validFrom = 0,
        validUntil = 17768026519L,
        credentialId = id,
    )

    private fun createMockKeyBinding(
        keyBindingIdentifier: String = KEY_BINDING_IDENTIFIER,
        keyBindingAlgorithm: String = KEY_BINDING_ALGORITHM.stdName,
    ) = CredentialKeyBindingEntity(
        id = keyBindingIdentifier,
        credentialId = CREDENTIAL_ID,
        algorithm = keyBindingAlgorithm,
        bindingType = KeyBindingType.HARDWARE,
    )

    private companion object {
        const val BUNDLE_ITEM_ID = 1L
        const val CREDENTIAL_ID = 1L
        const val CREDENTIAL_ID_2 = 2L
        const val KEY_BINDING_IDENTIFIER = "privateKeyIdentifier"
        const val KEY_BINDING_IDENTIFIER_2 = "privateKeyIdentifier2"
        const val PAYLOAD =
            "ewogICJ0eXAiOiJ2YytzZC1qd3QiLAogICJhbGciOiJFUzI1NiIsCiAgImtpZCI6ImtleUlkIgp9.ewogICJpc3MiOiJkaWQ6dGR3OmlkZW50aWZpZXIiLAogICJ2Y3QiOiJ2Y3QiCn0.ZXdvZ0lDSjBlWEFpT2lKMll5dHpaQzFxZDNRaUxBb2dJQ0poYkdjaU9pSkZVekkxTmlJc0NpQWdJbXRwWkNJNkltdGxlVWxrSWdwOS4uNHNwTXBzWE1nYlNyY0lqMFdNbXJNYXdhcVRzeG9GWmItcjdwTWlubEhvZklRRUhhS2pzV1J0dENzUTkyd0tfa3RpaDQta2VCdjdVbkc2MkRPa2NDbGc"
        const val PAYLOAD_2 =
            "ewogICJ0eXAiOiJ2YytzZC1qd3QiLAogICJhbGciOiJFUzI1NiIsCiAgImtpZCI6ImtleUlkIgp9.ewogICJpc3MiOiJkaWQ6dGR3OmlkZW50aWZpZXIiLAogICJ2Y3QiOiJ2Y3QiCn0.ZXdvZ0lDSjBlWEFpT2lKMll5dHpaQzFxZDNRaUxBb2dJQ0poYkdjaU9pSkZVekkxTmlJc0NpQWdJbXRwWkNJNkltdGxlVWxrSWdwOS4uNHNwTXBzWE1nYlNyY0lqMFdNbXJNYXdhcVRzeG9GWmItcjdwTWlubEhvZklRRUhhS2pzV1J0dENzUTkyd0tfa3RpaDQta2VCdjdVbkc2MkRPa2NDbGc"
        val KEY_BINDING_ALGORITHM = SigningAlgorithm.ES512
        val KEY_BINDING_ALGORITHM_2 = SigningAlgorithm.ES512
    }
}
