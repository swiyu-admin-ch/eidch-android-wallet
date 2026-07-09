package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.GetKeyPairForKeyBindingError
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.usecase.GetHardwareKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.GetSoftwareKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockKeyPairs.VALID_KEY_PAIR_HARDWARE
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import ch.admin.foitt.openid4vc.utils.Constants.ANDROID_KEY_STORE
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetKeyPairForKeyBindingImplTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var mockGetHardwareKeyPair: GetHardwareKeyPair

    @MockK
    private lateinit var mockGetSoftwareKeyPair: GetSoftwareKeyPair

    private lateinit var useCase: GetKeyPairForKeyBindingImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = GetKeyPairForKeyBindingImpl(
            getHardwareKeyPair = mockGetHardwareKeyPair,
            getSoftwareKeyPair = mockGetSoftwareKeyPair,
        )
    }

    @Test
    fun `software key binding returns software key pair`() = runTest(testDispatcher) {
        val keyBinding = KeyBinding(
            identifier = "software-key-id",
            algorithm = SigningAlgorithm.ES512,
            bindingType = KeyBindingType.SOFTWARE,
            publicKey = byteArrayOf(1, 2, 3),
            privateKey = byteArrayOf(4, 5, 6),
        )
        val expectedKeyPair = VALID_KEY_PAIR_HARDWARE.keyPair
        coEvery { mockGetSoftwareKeyPair(keyBinding.publicKey!!, keyBinding.privateKey!!) } returns Ok(expectedKeyPair)

        val result = useCase(keyBinding).assertOk()

        assertEquals(expectedKeyPair, result)
        coVerify(exactly = 1) { mockGetSoftwareKeyPair(keyBinding.publicKey!!, keyBinding.privateKey!!) }
        coVerify(exactly = 0) { mockGetHardwareKeyPair(any(), any()) }
    }

    @Test
    fun `hardware key binding returns hardware key pair`() = runTest(testDispatcher) {
        val keyBinding = KeyBinding(
            identifier = "hardware-key-id",
            algorithm = SigningAlgorithm.ES512,
            bindingType = KeyBindingType.HARDWARE,
        )
        val expectedKeyPair = VALID_KEY_PAIR_HARDWARE.keyPair
        coEvery { mockGetHardwareKeyPair(keyBinding.identifier, ANDROID_KEY_STORE) } returns Ok(expectedKeyPair)

        val result = useCase(keyBinding).assertOk()

        assertEquals(expectedKeyPair, result)
        coVerify(exactly = 1) { mockGetHardwareKeyPair(keyBinding.identifier, ANDROID_KEY_STORE) }
        coVerify(exactly = 0) { mockGetSoftwareKeyPair(any(), any()) }
    }

    @Test
    fun `software key binding without private key returns SoftwareKeyNotFound`() = runTest(testDispatcher) {
        val keyBinding = KeyBinding(
            identifier = "software-key-id",
            algorithm = SigningAlgorithm.ES512,
            bindingType = KeyBindingType.SOFTWARE,
            publicKey = byteArrayOf(1, 2, 3),
            privateKey = null,
        )

        useCase(keyBinding).assertErrorType(GetKeyPairForKeyBindingError.SoftwareKeyNotFound::class)

        coVerify(exactly = 0) { mockGetSoftwareKeyPair(any(), any()) }
        coVerify(exactly = 0) { mockGetHardwareKeyPair(any(), any()) }
    }
}
