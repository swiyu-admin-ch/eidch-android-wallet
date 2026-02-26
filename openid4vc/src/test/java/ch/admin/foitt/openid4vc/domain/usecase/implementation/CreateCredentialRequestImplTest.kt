@file:OptIn(UnsafeResultValueAccess::class)

package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.CredentialRequestType
import ch.admin.foitt.openid4vc.domain.model.CredentialType
import ch.admin.foitt.openid4vc.domain.model.VerifiableCredentialParams
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialRequestCredentialResponseEncryption
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.DeferredCredentialRequest
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.VerifiableCredentialRequest
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialRequestEncryption
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialResponseEncryption
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.VcSdJwtCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.jwe.JWEError
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwks
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionKeyPair
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionType
import ch.admin.foitt.openid4vc.domain.usecase.CreateCredentialRequest
import ch.admin.foitt.openid4vc.domain.usecase.jwe.CreateJWE
import ch.admin.foitt.openid4vc.util.SafeJsonTestInstance
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import com.nimbusds.jose.jwk.Curve.P_256
import com.nimbusds.jose.jwk.ECKey
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec

class CreateCredentialRequestImplTest {

    private val safeJson = SafeJsonTestInstance.safeJsonWithDiscriminator

    @MockK
    private lateinit var mockCreateJWE: CreateJWE

    @MockK
    private lateinit var mockVerifiableCredentialParams: VerifiableCredentialParams

    @MockK
    private lateinit var mockVcSdJwtCredentialConfiguration: VcSdJwtCredentialConfiguration

    private lateinit var useCase: CreateCredentialRequest

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        setupDefaultMocks()

        useCase = CreateCredentialRequestImpl(
            safeJson = safeJson,
            createJWE = mockCreateJWE,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `For no payload encryption and verifiable credential return a json credential request`() = runTest {
        val result = useCase(
            payloadEncryptionType = noPayloadEncryption,
            credentialType = verifiableCredentialType,
        ).assertOk()

        val expected = CredentialRequestType.Json(verifiableCredentialRequestJson)

        assertEquals(expected, result)
    }

    @Test
    fun `For no payload encryption and deferred credential return a json credential request`() = runTest {
        val result = useCase(
            payloadEncryptionType = noPayloadEncryption,
            credentialType = deferredCredentialType,
        ).assertOk()

        val expected = CredentialRequestType.Json(deferredCredentialRequestJson)

        assertEquals(expected, result)
    }

    @Test
    fun `For request encryption and verifiable credential return a jwt credential request`() = runTest {
        val result = useCase(
            payloadEncryptionType = requestEncryption,
            credentialType = verifiableCredentialType,
        ).assertOk()

        val expected = CredentialRequestType.Jwt("verifiable credential request jwe")

        assertEquals(expected, result)
    }

    @Test
    fun `For request encryption and deferred credential return a jwt credential request`() = runTest {
        val result = useCase(
            payloadEncryptionType = requestEncryption,
            credentialType = deferredCredentialType,
        ).assertOk()

        val expected = CredentialRequestType.Jwt("deferred credential request jwe")

        assertEquals(expected, result)
    }

    @Test
    fun `For response encryption and verifiable credential return a jwt credential request`() = runTest {
        val result = useCase(
            payloadEncryptionType = responseEncryption,
            credentialType = verifiableCredentialType,
        ).assertOk()

        val expected = CredentialRequestType.Jwt("verifiable credential request with response encryption jwe")

        assertEquals(expected, result)
    }

    @Test
    fun `For response encryption and deferred credential return a jwt credential request`() = runTest {
        val result = useCase(
            payloadEncryptionType = responseEncryption,
            credentialType = deferredCredentialType,
        ).assertOk()

        val expected = CredentialRequestType.Jwt("deferred credential request with response encryption jwe")

        assertEquals(expected, result)
    }

    @Test
    fun `Error during jwe creation is mapped`() = runTest {
        coEvery {
            mockCreateJWE(
                algorithm = ALG_VALUE,
                encryptionMethod = ENCRYPTION_VALUE,
                compressionAlgorithm = ZIP_VALUE,
                payload = verifiableCredentialRequestWithResponseEncryptionJson,
                encryptionKey = issuerPublicKeyJwk,
            )
        } returns Err(JWEError.Unexpected(IllegalStateException("jwe error")))

        useCase(
            payloadEncryptionType = responseEncryption,
            credentialType = verifiableCredentialType,
        ).assertErrorType(CredentialOfferError.Unexpected::class)
    }

    private fun setupDefaultMocks() {
        every { mockVerifiableCredentialParams.credentialConfiguration } returns mockVcSdJwtCredentialConfiguration
        every { mockVcSdJwtCredentialConfiguration.identifier } returns IDENTIFIER

        coEvery {
            mockCreateJWE(
                algorithm = ALG_VALUE,
                encryptionMethod = ENCRYPTION_VALUE,
                compressionAlgorithm = ZIP_VALUE,
                payload = verifiableCredentialRequestJson,
                encryptionKey = issuerPublicKeyJwk,
            )
        } returns Ok("verifiable credential request jwe")

        coEvery {
            mockCreateJWE(
                algorithm = ALG_VALUE,
                encryptionMethod = ENCRYPTION_VALUE,
                compressionAlgorithm = ZIP_VALUE,
                payload = deferredCredentialRequestJson,
                encryptionKey = issuerPublicKeyJwk,
            )
        } returns Ok("deferred credential request jwe")

        coEvery {
            mockCreateJWE(
                algorithm = ALG_VALUE,
                encryptionMethod = ENCRYPTION_VALUE,
                compressionAlgorithm = ZIP_VALUE,
                payload = verifiableCredentialRequestWithResponseEncryptionJson,
                encryptionKey = issuerPublicKeyJwk,
            )
        } returns Ok("verifiable credential request with response encryption jwe")

        coEvery {
            mockCreateJWE(
                algorithm = ALG_VALUE,
                encryptionMethod = ENCRYPTION_VALUE,
                compressionAlgorithm = ZIP_VALUE,
                payload = deferredCredentialRequestWithResponseEncryptionJson,
                encryptionKey = issuerPublicKeyJwk,
            )
        } returns Ok("deferred credential request with response encryption jwe")
    }

    val verifiableCredentialType by lazy {
        CredentialType.Verifiable(
            verifiableCredentialParams = mockVerifiableCredentialParams,
            proofs = null,
        )
    }

    val deferredCredentialType = CredentialType.Deferred(
        transactionId = TRANSACTION_ID
    )

    val noPayloadEncryption = PayloadEncryptionType.None

    val issuerPublicKeyJwk = Jwk(
        x = X_VALUE,
        y = Y_VALUE,
        crv = CURVE,
        kty = KEY_TYPE,
        kid = KEY_ID,
        alg = ALG_VALUE,
    )

    val credentialRequestEncryption = CredentialRequestEncryption(
        jwks = Jwks(
            keys = listOf(issuerPublicKeyJwk)
        ),
        encValuesSupported = listOf(ENCRYPTION_VALUE),
        zipValuesSupported = listOf(ZIP_VALUE),
        encryptionRequired = true
    )

    val requestEncryption = PayloadEncryptionType.Request(
        requestEncryption = credentialRequestEncryption,
    )

    val credentialResponseEncryption = CredentialResponseEncryption(
        algValuesSupported = listOf(ALG_VALUE),
        encValuesSupported = listOf(ENCRYPTION_VALUE),
        zipValuesSupported = listOf(ZIP_VALUE),
        encryptionRequired = true,
    )

    val walletKeyPair = createKeyPair()
    val walletPublicKey: ECKey = ECKey.Builder(P_256, walletKeyPair.public as ECPublicKey).build()

    val payloadEncryptionKeyPair by lazy {
        PayloadEncryptionKeyPair(
            keyPair = mockk<JWSKeyPair> {
                every { keyId } returns WALLET_PUBLIC_KEY_ID
                every { keyPair } returns walletKeyPair
            },
            alg = ALG_VALUE,
            enc = ENCRYPTION_VALUE,
            zip = ZIP_VALUE,
        )
    }

    val responseEncryption = PayloadEncryptionType.Response(
        requestEncryption = credentialRequestEncryption,
        responseEncryption = credentialResponseEncryption,
        responseEncryptionKeyPair = payloadEncryptionKeyPair,
    )

    val walletPublicKeyJwk = Jwk(
        kid = payloadEncryptionKeyPair.keyPair.keyId,
        kty = "EC",
        use = "enc",
        crv = P_256.name,
        alg = ALG_VALUE,
        x = walletPublicKey.x.toString(),
        y = walletPublicKey.y.toString(),
    )

    val credentialRequestCredentialResponseEncryption = CredentialRequestCredentialResponseEncryption(
        jwk = walletPublicKeyJwk,
        alg = payloadEncryptionKeyPair.alg,
        enc = payloadEncryptionKeyPair.enc,
        zip = payloadEncryptionKeyPair.zip,
    )

    val verifiableCredentialRequest = VerifiableCredentialRequest(
        credentialConfigurationId = IDENTIFIER,
        proofs = null,
        credentialResponseEncryption = null,
    )
    val verifiableCredentialRequestJson = safeJson.safeEncodeObjectToString(verifiableCredentialRequest).value

    val verifiableCredentialRequestWithResponseEncryption = VerifiableCredentialRequest(
        credentialConfigurationId = IDENTIFIER,
        proofs = null,
        credentialResponseEncryption = credentialRequestCredentialResponseEncryption,
    )
    val verifiableCredentialRequestWithResponseEncryptionJson = safeJson.safeEncodeObjectToString(
        verifiableCredentialRequestWithResponseEncryption
    ).value

    val deferredCredentialRequest = DeferredCredentialRequest(
        transactionId = TRANSACTION_ID,
        credentialResponseEncryption = null,
    )
    val deferredCredentialRequestJson = safeJson.safeEncodeObjectToString(deferredCredentialRequest).value

    val deferredCredentialRequestWithResponseEncryption = DeferredCredentialRequest(
        transactionId = TRANSACTION_ID,
        credentialResponseEncryption = credentialRequestCredentialResponseEncryption,
    )
    val deferredCredentialRequestWithResponseEncryptionJson = safeJson.safeEncodeObjectToString(
        deferredCredentialRequestWithResponseEncryption
    ).value

    private fun createKeyPair(): KeyPair {
        val generator = KeyPairGenerator.getInstance("EC")
        val spec = ECGenParameterSpec("secp256r1")
        generator.initialize(spec)
        return generator.generateKeyPair()
    }

    private companion object {
        const val IDENTIFIER = "identifier"
        const val TRANSACTION_ID = "transactionId"
        const val X_VALUE = "x value"
        const val Y_VALUE = "y value"
        const val CURVE = "curve"
        const val KEY_TYPE = "key type"
        const val KEY_ID = "key id"
        const val ENCRYPTION_VALUE = "A128GCM"
        const val ZIP_VALUE = "DEF"
        const val ALG_VALUE = "alg value"
        const val WALLET_PUBLIC_KEY_ID = "wallet public key id"
    }
}
