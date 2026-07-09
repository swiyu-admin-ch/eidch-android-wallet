package ch.admin.foitt.wallet.platform.credential.domain.model

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.Validity
import ch.admin.foitt.openid4vc.domain.model.anycredential.getValidity
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialDisplayStatus
import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemWithKeyBinding
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialKeyBindingEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithBundleItemsWithKeyBinding
import com.github.michaelbull.result.BindingScope
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import java.time.Instant

internal fun VerifiableCredentialWithBundleItemsWithKeyBinding.toAnyCredentials(): Result<List<AnyCredential>, AnyCredentialError> =
    binding {
        bundleItemsWithKeyBinding.map { bundleItemWithKeyBinding ->
            bundleItemWithKeyBindingToAnyCredential(
                credential = credential,
                verifiableCredential = verifiableCredential,
                bundleItemWithKeyBinding = bundleItemWithKeyBinding
            )
        }
    }

internal fun VerifiableCredentialWithBundleItemsWithKeyBinding.toNextAnyCredentialToPresent(): Result<AnyCredential, AnyCredentialError> =
    binding {
        bundleItemWithKeyBindingToAnyCredential(
            credential = credential,
            verifiableCredential = verifiableCredential,
            bundleItemWithKeyBinding = nextBundleItemWithKeyBindingToPresent
        )
    }

private fun BindingScope<AnyCredentialError>.bundleItemWithKeyBindingToAnyCredential(
    credential: Credential,
    verifiableCredential: VerifiableCredentialEntity,
    bundleItemWithKeyBinding: BundleItemWithKeyBinding
): VcSdJwtCredential = runSuspendCatching {
    when (credential.format) {
        CredentialFormat.DC_SD_JWT, CredentialFormat.VC_SD_JWT -> VcSdJwtCredential(
            id = verifiableCredential.credentialId,
            keyBinding = bundleItemWithKeyBinding.keyBinding?.toKeyBinding()
                ?.mapError(KeyBindingError::toAnyCredentialError)
                ?.bind(),
            payload = bundleItemWithKeyBinding.bundleItem.payload,
            validFrom = verifiableCredential.validFrom,
            validUntil = verifiableCredential.validUntil,
            format = credential.format,
        )

        CredentialFormat.UNKNOWN -> error("Unsupported credential format")
    }
}.mapError { throwable ->
    throwable.toAnyCredentialError("Credential.toAnyCredential() error")
}.bind()

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

internal fun VerifiableCredentialEntity.getDisplayStatus(
    status: CredentialStatus,
    businessExpiryInstant: Instant? = null
): CredentialDisplayStatus {
    val validity = getValidity(validFrom, validUntil, businessExpiryInstant)
    return status.getDisplayStatus(validity, businessExpiryInstant)
}

private fun CredentialStatus.getDisplayStatus(validity: Validity, businessExpiryInstant: Instant?) = when (validity) {
    // JWT expiry and notYetValid take priority
    is Validity.Expired -> CredentialDisplayStatus.Expired(validity.expiredAt)
    is Validity.NotYetValid -> if (this == CredentialStatus.REVOKED) {
        this.toDisplayStatus()
    } else {
        CredentialDisplayStatus.NotYetValid(validity.validFrom)
    }
    is Validity.BusinessExpired -> CredentialDisplayStatus.BusinessExpired(businessExpiryInstant)
    Validity.Valid -> this.toDisplayStatus()
}

internal fun CredentialStatus.toDisplayStatus() = when (this) {
    CredentialStatus.VALID -> CredentialDisplayStatus.Valid
    CredentialStatus.REVOKED -> CredentialDisplayStatus.Revoked
    CredentialStatus.SUSPENDED -> CredentialDisplayStatus.Suspended
    CredentialStatus.UNSUPPORTED -> CredentialDisplayStatus.Unsupported
    CredentialStatus.UNKNOWN -> CredentialDisplayStatus.Unknown
}
