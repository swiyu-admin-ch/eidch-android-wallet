package ch.admin.foitt.wallet.platform.credential.domain.model

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.Validity
import ch.admin.foitt.openid4vc.domain.model.anycredential.getValidity
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialDisplayStatus
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialKeyBindingEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithBundleItemsWithKeyBinding
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError

internal fun VerifiableCredentialWithBundleItemsWithKeyBinding.toAnyCredentials():
    Result<List<AnyCredential>, AnyCredentialError> = binding {
    val verifiableCredential = this@toAnyCredentials.verifiableCredential
    val credential = this@toAnyCredentials.credential

    bundleItemsWithKeyBinding.map { bundleItemWithKeyBinding ->
        runSuspendCatching {
            when (credential.format) {
                CredentialFormat.VC_SD_JWT -> VcSdJwtCredential(
                    id = verifiableCredential.credentialId,
                    keyBinding = bundleItemWithKeyBinding.keyBinding?.toKeyBinding()
                        ?.mapError(KeyBindingError::toAnyCredentialError)
                        ?.bind(),
                    payload = bundleItemWithKeyBinding.bundleItem.payload,
                    validFrom = verifiableCredential.validFrom,
                    validUntil = verifiableCredential.validUntil,
                )

                CredentialFormat.UNKNOWN -> error("Unsupported credential format")
            }
        }.mapError { throwable ->
            throwable.toAnyCredentialError("Credential.toAnyCredential() error")
        }.bind()
    }
}

fun CredentialKeyBindingEntity.toKeyBinding(): Result<KeyBinding, KeyBindingError> = binding {
    val signingAlgorithm = runSuspendCatching {
        SigningAlgorithm.valueOf(algorithm)
    }.mapError { throwable ->
        throwable.toKeyBindingError("SigningAlgorithm.valueOf($algorithm) error")
    }.bind()

    KeyBinding(
        identifier = id,
        algorithm = signingAlgorithm,
        bindingType = bindingType,
        publicKey = publicKey,
        privateKey = privateKey,
    )
}

internal fun VerifiableCredentialEntity.getDisplayStatus(status: CredentialStatus): CredentialDisplayStatus {
    val validity = getValidity(validFrom, validUntil)
    return status.getDisplayStatus(validity)
}

private fun CredentialStatus.getDisplayStatus(validity: Validity) = when (validity) {
    // Priority is given to local state
    is Validity.Expired -> CredentialDisplayStatus.Expired(validity.expiredAt)
    is Validity.NotYetValid -> CredentialDisplayStatus.NotYetValid(validity.validFrom)
    Validity.Valid -> this.toDisplayStatus()
}

internal fun CredentialStatus.toDisplayStatus() = when (this) {
    CredentialStatus.VALID -> CredentialDisplayStatus.Valid
    CredentialStatus.REVOKED -> CredentialDisplayStatus.Revoked
    CredentialStatus.SUSPENDED -> CredentialDisplayStatus.Suspended
    CredentialStatus.UNSUPPORTED -> CredentialDisplayStatus.Unsupported
    CredentialStatus.UNKNOWN -> CredentialDisplayStatus.Unknown
}
