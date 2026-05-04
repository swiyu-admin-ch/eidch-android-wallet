package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.GetAllAnyCredentialsByCredentialIdError
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

class GetAllAnyCredentialsByCredentialIdImplTest {

    @MockK
    private lateinit var mockCredentialWithKeyBindingRepository:
        VerifiableCredentialWithBundleItemsWithKeyBindingRepository

    private lateinit var useCase: GetAllAnyCredentialsByCredentialIdImpl

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
    fun `returns list with vc+sd_jwt credential`() = runTest {
        val mockCredential = createMockCredentialWithKeyBinding()
        coEvery { mockCredentialWithKeyBindingRepository.getByCredentialId(CREDENTIAL_ID) } returns Ok(mockCredential)

        val anyCredentials = useCase(CREDENTIAL_ID).assertOk()

        assertTrue(anyCredentials.first() is VcSdJwtCredential)
        val anyCredential = anyCredentials.first() as VcSdJwtCredential
        assertEquals(CREDENTIAL_ID, anyCredential.id)
        assertEquals(PAYLOAD_1, anyCredential.payload)
        assertNotNull(anyCredential.keyBinding)
        assertEquals(KEY_BINDING_ID, anyCredential.keyBinding?.identifier)
        assertEquals(KEY_BINDING_ALGORITHM, anyCredential.keyBinding?.algorithm)
    }

    @Test
    fun `returns list with multiple bundle items`() = runTest {
        val mockCredential = createMockCredentialWithKeyBinding(multipleBundleItems = true)
        coEvery { mockCredentialWithKeyBindingRepository.getByCredentialId(CREDENTIAL_ID) } returns Ok(mockCredential)

        val anyCredentials = useCase(CREDENTIAL_ID).assertOk()

        assertEquals(2, anyCredentials.size)
        assertTrue(anyCredentials[0] is VcSdJwtCredential)
        assertTrue(anyCredentials[1] is VcSdJwtCredential)
        val first = anyCredentials[0] as VcSdJwtCredential
        val second = anyCredentials[1] as VcSdJwtCredential
        assertEquals(PAYLOAD_1, first.payload)
        assertEquals(PAYLOAD_2, second.payload)
    }

    @Test
    fun `repository error is mapped`() = runTest {
        val exception = IllegalStateException("no credential found")
        coEvery {
            mockCredentialWithKeyBindingRepository.getByCredentialId(any())
        } returns Err(SsiError.Unexpected(exception))

        useCase(CREDENTIAL_ID).assertErrorType(GetAllAnyCredentialsByCredentialIdError::class)
    }

    @Test
    fun `unsupported credential format returns error`() = runTest {
        val mockCredential = createMockCredentialWithKeyBinding(format = CredentialFormat.UNKNOWN)
        coEvery { mockCredentialWithKeyBindingRepository.getByCredentialId(CREDENTIAL_ID) } returns Ok(mockCredential)

        val result = useCase(CREDENTIAL_ID)

        result.assertErrorType(CredentialError.Unexpected::class)
    }

    @Test
    fun `unknown signing algorithm returns error`() = runTest {
        val mockCredential = createMockCredentialWithKeyBinding(keyBindingAlgorithm = "other")
        coEvery {
            mockCredentialWithKeyBindingRepository.getByCredentialId(CREDENTIAL_ID)
        } returns Ok(mockCredential)

        val result = useCase(CREDENTIAL_ID)

        result.assertErrorType(CredentialError.Unexpected::class)
    }

    private fun createMockCredentialWithKeyBinding(
        format: CredentialFormat = CredentialFormat.VC_SD_JWT,
        keyBindingAlgorithm: String = KEY_BINDING_ALGORITHM.stdName,
        multipleBundleItems: Boolean = false,
    ) = VerifiableCredentialWithBundleItemsWithKeyBinding(
        credential = createMockCredential(format),
        verifiableCredential = createMockVerifiableCredential(),
        bundleItemsWithKeyBinding = if (multipleBundleItems) {
            listOf(
                BundleItemWithKeyBinding(
                    bundleItem = createMockBundleItem(id = BUNDLE_ITEM_ID, payload = PAYLOAD_1),
                    keyBinding = createMockKeyBinding(keyBindingAlgorithm),
                ),
                BundleItemWithKeyBinding(
                    bundleItem = createMockBundleItem(id = BUNDLE_ITEM_ID + 1, payload = PAYLOAD_2),
                    keyBinding = createMockKeyBinding(keyBindingAlgorithm),
                ),
            )
        } else {
            listOf(
                BundleItemWithKeyBinding(
                    bundleItem = createMockBundleItem(id = BUNDLE_ITEM_ID, payload = PAYLOAD_1),
                    keyBinding = createMockKeyBinding(keyBindingAlgorithm),
                )
            )
        },
    )

    private fun createMockBundleItem(id: Long, payload: String) = BundleItemEntity(
        id = id,
        credentialId = CREDENTIAL_ID,
        payload = payload
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
        const val PAYLOAD_1 = "ewogICJ0eXAiOiJ2YytzZC1qd3QiLAogICJhbGciOiJFUzI1NiIsCiAgImtpZCI6ImtleUlkIgp9." +
            "ewogICJpc3MiOiJkaWQ6dGR3OmlkZW50aWZpZXIiLAogICJ2Y3QiOiJ2Y3QiCn0." +
            "ZXdvZ0lDSjBlWEFpT2lKMll5dHpaQzFxZDNRaUxBb2dJQ0poYkdjaU9pSkZVekkxTmlJc0NpQWdJbXRwWkNJNkltdGxlVWxrSWdwOS4uNHNwTXBzWE1" +
            "nYlNyY0lqMFdNbXJNYXdhcVRzeG9GWmItcjdwTWlubEhvZklRRUhhS2pzV1J0dENzUTkyd0tfa3RpaDQta2VCdjdVbkc2MkRPa2NDbGc~"
        const val PAYLOAD_2 = PAYLOAD_1 // payload content not relevant for the test logic
        val KEY_BINDING_ALGORITHM = SigningAlgorithm.ES512
    }
}
