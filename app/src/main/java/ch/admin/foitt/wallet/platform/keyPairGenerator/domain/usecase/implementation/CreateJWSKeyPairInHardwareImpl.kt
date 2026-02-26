package ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.implementation

import android.content.Context
import android.content.pm.PackageManager
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import ch.admin.foitt.openid4vc.di.DefaultDispatcher
import ch.admin.foitt.openid4vc.domain.model.KeyStorageSecurityLevel
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model.CreateJWSKeyPairError
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model.CreateKeyGenSpecError
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model.KeyPairError
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model.toCreateJWSKeyPairError
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.CreateJWSKeyPairInHardware
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.CreateKeyGenSpec
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.getOr
import com.github.michaelbull.result.getOrThrow
import com.github.michaelbull.result.mapError
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.util.UUID
import javax.inject.Inject

internal class CreateJWSKeyPairInHardwareImpl @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val createKeyGenSpec: CreateKeyGenSpec,
) : CreateJWSKeyPairInHardware {
    override suspend operator fun invoke(
        keyAlias: String?,
        signingAlgorithm: SigningAlgorithm,
        provider: String,
        keyStorageSecurityLevels: List<KeyStorageSecurityLevel>?,
        attestationChallenge: ByteArray?,
    ): Result<JWSKeyPair, CreateJWSKeyPairError> = withContext(defaultDispatcher) {
        coroutineBinding {
            val keyId = getKeyId(provider, keyAlias)
                .mapError { throwable ->
                    KeyPairError.Unexpected(throwable)
                }.bind()

            val keyPair = createKeyPair(
                keyId = keyId,
                signingAlgorithm = signingAlgorithm,
                provider = provider,
                keyStorageSecurityLevels = keyStorageSecurityLevels,
                attestationChallenge = attestationChallenge,
            ).bind()

            JWSKeyPair(
                algorithm = signingAlgorithm,
                keyPair = keyPair,
                keyId = keyId,
                bindingType = KeyBindingType.HARDWARE,
            )
        }
    }

    private fun getKeyId(
        provider: String,
        keyAlias: String?
    ) = runSuspendCatching {
        val keyStore = KeyStore.getInstance(provider)
        keyStore.load(null)
        val keyId = keyAlias ?: generateKeyId(keyStore).getOrThrow()

        if (keyStore.getEntry(keyId, null) != null) {
            keyStore.deleteEntry(keyId)
        }

        keyId
    }

    private fun generateKeyId(keyStore: KeyStore): Result<String, Throwable> {
        // A collision is nearly impossible, but if it happens, the overridden keypair and linked credential is lost.
        // So we check if some key already exists here
        repeat(KEY_ID_RETRIES) {
            val keyId = UUID.randomUUID().toString()
            val isNewEntry = runSuspendCatching<Boolean> {
                keyStore.getEntry(keyId, null) == null
            }.getOr(true)
            if (isNewEntry) return Ok(keyId)
            Timber.w(collisionMessage)
        }
        return Err(Exception("Fatal $collisionMessage"))
    }

    private fun createKeyPair(
        keyId: String,
        signingAlgorithm: SigningAlgorithm,
        provider: String,
        keyStorageSecurityLevels: List<KeyStorageSecurityLevel>?,
        attestationChallenge: ByteArray?,
    ): Result<KeyPair, CreateJWSKeyPairError> = binding {
        val useStrongBox = useStrongBox(keyStorageSecurityLevels).bind()
        val spec = createKeyGenSpec(
            keyId = keyId,
            signingAlgorithm = signingAlgorithm,
            useStrongBox = useStrongBox,
            attestationChallenge = attestationChallenge,
        ).mapError(CreateKeyGenSpecError::toCreateJWSKeyPairError)
            .bind()

        val keyPair = generateKeyPair(signingAlgorithm, provider, spec)
            .mapError { throwable ->
                throwable.toCreateJWSKeyPairError("error when creating key pair")
            }.bind()

        keyPair
    }

    private fun useStrongBox(keyStorageSecurityLevels: List<KeyStorageSecurityLevel>?): Result<Boolean, CreateJWSKeyPairError> = binding {
        if (keyStorageSecurityLevels.isNullOrEmpty()) {
            return@binding isStrongBoxAvailable()
        }

        val keyStorageOrdered = keyStorageSecurityLevels.sortedBy { it.priority }
        val supportedKeyStorage = listOf(KeyStorageSecurityLevel.HIGH, KeyStorageSecurityLevel.ENHANCED_BASIC)
        val keyStorageFiltered = keyStorageOrdered.filter { it in supportedKeyStorage }

        if (keyStorageFiltered.isEmpty()) {
            return@binding Err(KeyPairError.UnsupportedProofKeyStorageSecurityLevel).bind<Boolean>()
        } else {
            keyStorageFiltered.forEach {
                when {
                    it == KeyStorageSecurityLevel.HIGH && isStrongBoxAvailable() -> return@binding true
                    it == KeyStorageSecurityLevel.ENHANCED_BASIC -> return@binding false
                }
            }

            return@binding Err(KeyPairError.IncompatibleDeviceProofKeyStorage).bind<Boolean>()
        }
    }

    private fun isStrongBoxAvailable(): Boolean {
        return appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
    }

    private fun generateKeyPair(
        signingAlgorithm: SigningAlgorithm,
        provider: String,
        spec: KeyGenParameterSpec
    ): Result<KeyPair, Throwable> = runSuspendCatching {
        val generator = KeyPairGenerator.getInstance(signingAlgorithm.toKeyAlgorithm(), provider)
        generator.initialize(spec)
        generator.generateKeyPair()
    }

    private fun SigningAlgorithm.toKeyAlgorithm() = when (this) {
        SigningAlgorithm.ES256,
        SigningAlgorithm.ES512 -> KeyProperties.KEY_ALGORITHM_EC
    }

    companion object {
        private const val KEY_ID_RETRIES: Int = 5
        private val collisionMessage by lazy { "Collision while creating a key Id" }
    }
}
