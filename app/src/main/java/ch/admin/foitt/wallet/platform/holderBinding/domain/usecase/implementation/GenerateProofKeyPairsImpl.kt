package ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.KeyStorageSecurityLevel
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.ProofTypeConfig
import ch.admin.foitt.openid4vc.domain.model.keyBinding.BindingKeyPair
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.RequestKeyAttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.RequestKeyAttestation
import ch.admin.foitt.wallet.platform.holderBinding.domain.model.GenerateProofKeyPairError
import ch.admin.foitt.wallet.platform.holderBinding.domain.model.HolderBindingError
import ch.admin.foitt.wallet.platform.holderBinding.domain.model.toGenerateProofKeyPairError
import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.GenerateProofKeyPairs
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model.CreateJWSKeyPairError
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.CreateJWSKeyPairInSoftware
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class GenerateProofKeyPairsImpl @Inject constructor(
    private val createJWSKeyPairInSoftware: CreateJWSKeyPairInSoftware,
    private val requestKeyAttestation: RequestKeyAttestation,
) : GenerateProofKeyPairs {

    override suspend fun invoke(
        amount: Int,
        proofTypeConfig: ProofTypeConfig,
    ): Result<List<BindingKeyPair>, GenerateProofKeyPairError> = coroutineBinding {
        val cryptographicSuite = getCryptographicSuite(proofTypeConfig).bind()

        buildList {
            repeat(amount) {
                add(
                    when (val keyAttestationConfig = proofTypeConfig.keyAttestationsRequired) {
                        null -> createSoftwareKeyPair(cryptographicSuite).bind()
                        else -> createHardwareKeyPairWithKeyAttestation(
                            signingAlgorithm = cryptographicSuite,
                            keyStorageSecurityLevels = keyAttestationConfig.keyStorage
                        ).bind()
                    }
                )
            }
        }
    }

    private fun getCryptographicSuite(
        proofTypeConfig: ProofTypeConfig,
    ): Result<SigningAlgorithm, HolderBindingError.UnsupportedCryptographicSuite> {
        val cryptographicSuite = proofTypeConfig.proofSigningAlgValuesSupported
        val preferredSigningAlgorithms = getPreferredSigningAlgorithms()
        // algorithms from our preference list have priority over the algorithms from the issuer
        val signingAlgorithms = preferredSigningAlgorithms.intersect(cryptographicSuite.toSet())

        return when {
            signingAlgorithms.isEmpty() -> Err(HolderBindingError.UnsupportedCryptographicSuite)
            else -> Ok(signingAlgorithms.first())
        }
    }

    // priority list of preferred signing algorithms (first position = highest priority)
    private fun getPreferredSigningAlgorithms() = listOf(SigningAlgorithm.ES256)

    private suspend fun createSoftwareKeyPair(
        signingAlgorithm: SigningAlgorithm
    ): Result<BindingKeyPair, GenerateProofKeyPairError> = coroutineBinding {
        val keyPair = createJWSKeyPairInSoftware(signingAlgorithm = signingAlgorithm)
            .mapError(CreateJWSKeyPairError::toGenerateProofKeyPairError)
            .bind()

        BindingKeyPair(
            keyPair = keyPair,
            attestationJwt = null
        )
    }

    private suspend fun createHardwareKeyPairWithKeyAttestation(
        signingAlgorithm: SigningAlgorithm,
        keyStorageSecurityLevels: List<KeyStorageSecurityLevel>?,
    ): Result<BindingKeyPair, GenerateProofKeyPairError> = coroutineBinding {
        val keyPairWithAttestation = requestKeyAttestation(
            keyAlias = null,
            signingAlgorithm = signingAlgorithm,
            keyStorageSecurityLevels = keyStorageSecurityLevels,
        ).mapError(RequestKeyAttestationError::toGenerateProofKeyPairError)
            .bind()

        BindingKeyPair(
            keyPair = keyPairWithAttestation.keyPair,
            attestationJwt = keyPairWithAttestation.attestation,
        )
    }
}
