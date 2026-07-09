package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.GetKeyPairForKeyBindingError
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.TokenType
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.usecase.GetKeyPairForKeyBinding
import ch.admin.foitt.wallet.platform.credential.domain.model.GetBindingKeyPairError
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialAuthenticationEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialAuthenticationWithDpopBinding
import ch.admin.foitt.wallet.platform.database.domain.model.DpopBindingEntity
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import ch.admin.foitt.wallet.util.assertOkNullable
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.security.KeyPair

class GetBindingKeyPairImplTest {

    @MockK
    private lateinit var mockGetKeyPairForKeyBinding: GetKeyPairForKeyBinding

    private lateinit var useCase: GetBindingKeyPairImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = GetBindingKeyPairImpl(
            getKeyPairForKeyBinding = mockGetKeyPairForKeyBinding
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `No dpop binding returns null`() = runTest {
        useCase(authentication = authentication()).assertOkNullable()
    }

    @Test
    fun `Hardware key not found is mapped`() = runTest {
        coEvery { mockGetKeyPairForKeyBinding(any()) } returns Err(GetKeyPairForKeyBindingError.HardwareKeyNotFound)

        useCase(
            authentication = authentication(
                dpopBinding = dpopBindingEntity(bindingType = KeyBindingType.HARDWARE)
            )
        ).assertErrorType(GetBindingKeyPairError.HardwareKeyNotFound::class)
    }

    @Test
    fun `Software binding returns binding key pair`() = runTest {
        val publicKey = byteArrayOf(1, 2, 3)
        val privateKey = byteArrayOf(4, 5, 6)
        val softwareKeyPair = io.mockk.mockk<KeyPair>(relaxed = true)
        val bindingType = KeyBindingType.SOFTWARE
        val algorithm = ALGORITHM
        val keyBinding = KeyBinding(
            identifier = KEY_ID,
            algorithm = algorithm,
            bindingType = bindingType,
            publicKey = publicKey,
            privateKey = privateKey
        )

        coEvery { mockGetKeyPairForKeyBinding(keyBinding) } returns Ok(softwareKeyPair)

        val result = useCase(
            authentication = authentication(
                dpopBinding = dpopBindingEntity(
                    bindingType = bindingType,
                    publicKey = publicKey,
                    privateKey = privateKey,
                )
            )
        ).assertOk()

        checkNotNull(result)
        assertEquals(algorithm, result.keyPair.algorithm)
        assertEquals(bindingType, result.keyPair.bindingType)
    }

    private fun authentication(
        dpopBinding: DpopBindingEntity? = null,
    ) = CredentialAuthenticationWithDpopBinding(
        credentialAuthentication = CredentialAuthenticationEntity(
            credentialId = 1,
            tokenType = TokenType.BEARER,
            accessToken = "access-token",
            refreshToken = "refresh-token",
        ),
        dpopBinding = dpopBinding,
    )

    private fun dpopBindingEntity(
        bindingType: KeyBindingType,
        publicKey: ByteArray? = null,
        privateKey: ByteArray? = null,
    ) = DpopBindingEntity(
        id = KEY_ID,
        credentialAuthenticationId = 1,
        algorithm = ALGORITHM.name,
        bindingType = bindingType,
        publicKey = publicKey,
        privateKey = privateKey,
    )

    companion object {
        private const val KEY_ID = "key-id"
        private val ALGORITHM = SigningAlgorithm.ES256
    }
}
