package ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialResponseEncryption
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionKeyPair
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model.KeyPairError
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.CreateJWSKeyPairInSoftware
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.PayloadEncryptionError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.CreatePayloadEncryptionKeyPair
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CreatePayloadEncryptionKeyPairImplTest {
    @MockK
    private lateinit var mockCreateJWSKeyPairInSoftware: CreateJWSKeyPairInSoftware

    @MockK
    private lateinit var mockJwsKeyPair: JWSKeyPair

    private lateinit var useCase: CreatePayloadEncryptionKeyPair

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = CreatePayloadEncryptionKeyPairImpl(
            createJWSKeyPairInSoftware = mockCreateJWSKeyPairInSoftware,
        )

        coEvery { mockCreateJWSKeyPairInSoftware(SigningAlgorithm.ES256) } returns Ok(mockJwsKeyPair)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Creating payload encryption key pair returns success`() = runTest {
        val result = useCase(credentialResponseEncryption).assertOk()

        val expected = PayloadEncryptionKeyPair(
            keyPair = mockJwsKeyPair,
            alg = ALGORITHM,
            enc = ENCRYPTION,
            zip = ZIP,
        )

        assertEquals(expected, result)
    }

    @Test
    fun `Error during payload encryption key pair creation returns error`() = runTest {
        coEvery {
            mockCreateJWSKeyPairInSoftware(SigningAlgorithm.ES256)
        } returns Err(KeyPairError.IncompatibleDeviceProofKeyStorage)

        useCase(credentialResponseEncryption).assertErrorType(PayloadEncryptionError.IncompatibleDeviceProofKeyStorage::class)
    }

    private companion object {
        const val ALGORITHM = "ECDH-ES"
        const val ENCRYPTION = "A128GCM"
        const val ZIP = "DEF"
        val credentialResponseEncryption = CredentialResponseEncryption(
            algValuesSupported = listOf(ALGORITHM),
            encValuesSupported = listOf(ENCRYPTION),
            zipValuesSupported = listOf(ZIP),
            encryptionRequired = true,
        )
    }
}
