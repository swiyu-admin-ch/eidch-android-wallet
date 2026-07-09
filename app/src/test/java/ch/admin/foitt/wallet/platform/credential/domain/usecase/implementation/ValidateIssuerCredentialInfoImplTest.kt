package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialRequestEncryption
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialResponseEncryption
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwks
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.EncryptionAlgorithm
import ch.admin.foitt.wallet.platform.credential.domain.usecase.ValidateIssuerCredentialInfo
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ValidateIssuerCredentialInfoImplTest {

    @MockK
    private lateinit var mockIssuerCredentialInfo: IssuerCredentialInfo

    @MockK
    private lateinit var mockRequestEncryption: CredentialRequestEncryption

    @MockK
    private lateinit var mockResponseEncryption: CredentialResponseEncryption

    @MockK
    private lateinit var mockJwks: Jwks

    @MockK
    private lateinit var mockJwk: Jwk

    private lateinit var useCase: ValidateIssuerCredentialInfo

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = ValidateIssuerCredentialInfoImpl()

        defaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Valid config returns success`() = runTest {
        assertTrue(useCase(mockIssuerCredentialInfo))
    }

    @Test
    fun `Config with response encryption but without request encryption returns an error`() = runTest {
        every { mockIssuerCredentialInfo.credentialRequestEncryption } returns null

        assertFalse(useCase(mockIssuerCredentialInfo))
    }

    @Test
    fun `Response encryption contains unsupported algorithm`() = runTest {
        every { mockResponseEncryption.algValuesSupported } returns listOf(SUPPORTED_ALGORITHM, OTHER_ALGORITHM)

        assertFalse(useCase(mockIssuerCredentialInfo))
    }

    @Test
    fun `Response encryption contains supported and unsupported encryption`() = runTest {
        every { mockResponseEncryption.encValuesSupported } returns listOf(OTHER_ENCRYPTION, SUPPORTED_ENCRYPTION_2)

        assertTrue(useCase(mockIssuerCredentialInfo))
    }

    @Test
    fun `Response encryption contains only unsupported encryption`() = runTest {
        every { mockResponseEncryption.encValuesSupported } returns listOf(OTHER_ENCRYPTION)

        assertFalse(useCase(mockIssuerCredentialInfo))
    }

    @Test
    fun `Response encryption contains unsupported zip value`() = runTest {
        every { mockResponseEncryption.zipValuesSupported } returns listOf(SUPPORTED_ZIP_VALUE, OTHER_ZIP_VALUE)

        assertFalse(useCase(mockIssuerCredentialInfo))
    }

    @Test
    fun `Request encryption contains supported and unsupported encryption`() = runTest {
        every { mockRequestEncryption.encValuesSupported } returns listOf(OTHER_ENCRYPTION, SUPPORTED_ENCRYPTION_2)

        assertTrue(useCase(mockIssuerCredentialInfo))
    }

    @Test
    fun `Request encryption contains only unsupported encryption`() = runTest {
        every { mockRequestEncryption.encValuesSupported } returns listOf(OTHER_ENCRYPTION)

        assertFalse(useCase(mockIssuerCredentialInfo))
    }

    @Test
    fun `Request encryption contains unsupported zip value`() = runTest {
        every { mockRequestEncryption.zipValuesSupported } returns listOf(SUPPORTED_ZIP_VALUE, OTHER_ZIP_VALUE)

        assertFalse(useCase(mockIssuerCredentialInfo))
    }

    @Test
    fun `Request encryption contains jwk with unsupported curve`() = runTest {
        every { mockJwk.crv } returns OTHER_CURVE

        assertFalse(useCase(mockIssuerCredentialInfo))
    }

    @Test
    fun `Request encryption contains jwk with unsupported algorithm`() = runTest {
        every { mockJwk.alg } returns OTHER_ALGORITHM

        assertFalse(useCase(mockIssuerCredentialInfo))
    }

    private fun defaultMocks() {
        every { mockIssuerCredentialInfo.credentialRequestEncryption } returns mockRequestEncryption
        every { mockIssuerCredentialInfo.credentialResponseEncryption } returns mockResponseEncryption

        every { mockRequestEncryption.jwks } returns mockJwks
        every { mockRequestEncryption.encValuesSupported } returns listOf(SUPPORTED_ENCRYPTION_1, SUPPORTED_ENCRYPTION_2)
        every { mockRequestEncryption.zipValuesSupported } returns listOf(SUPPORTED_ZIP_VALUE)

        every { mockJwks.keys } returns listOf(mockJwk)

        every { mockJwk.crv } returns SUPPORTED_CURVE
        every { mockJwk.alg } returns SUPPORTED_ALGORITHM

        every { mockResponseEncryption.algValuesSupported } returns listOf(SUPPORTED_ALGORITHM)
        every { mockResponseEncryption.encValuesSupported } returns listOf(SUPPORTED_ENCRYPTION_1, SUPPORTED_ENCRYPTION_2)
        every { mockResponseEncryption.zipValuesSupported } returns listOf(SUPPORTED_ZIP_VALUE)
    }

    private companion object {
        const val SUPPORTED_ALGORITHM = "ECDH-ES"
        const val OTHER_ALGORITHM = "other algorithm"
        val SUPPORTED_ENCRYPTION_1 = EncryptionAlgorithm.A128GCM.name
        val SUPPORTED_ENCRYPTION_2 = EncryptionAlgorithm.A256GCM.name
        const val OTHER_ENCRYPTION = "other encryption"
        const val SUPPORTED_ZIP_VALUE = "DEF"
        const val OTHER_ZIP_VALUE = "other zip value"
        const val SUPPORTED_CURVE = "P-256"
        const val OTHER_CURVE = "other curve"
    }
}
