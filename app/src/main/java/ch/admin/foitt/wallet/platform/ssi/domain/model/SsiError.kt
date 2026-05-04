@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.ssi.domain.model

import ch.admin.foitt.wallet.platform.batch.domain.error.RefreshBatchCredentialsError
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.MapToCredentialDisplayDataError
import ch.admin.foitt.wallet.platform.keybindingMatching.domain.model.MatchKeyBindingToPayloadCnfError
import timber.log.Timber

interface SsiError {
    data class Unexpected(val cause: Throwable?) :
        CredentialClaimDisplayRepositoryError,
        CredentialClaimRepositoryError,
        CredentialIssuerDisplayRepositoryError,
        CredentialRepositoryError,
        BundleItemRepositoryError,
        VerifiableCredentialRepositoryError,
        CredentialWithDisplaysRepositoryError,
        DeleteCredentialError,
        DeleteBundleItemError,
        MapToCredentialClaimDataError,
        CredentialOfferRepositoryError,
        GetCredentialDetailFlowError,
        GetCredentialsWithDetailsFlowError,
        CredentialWithKeyBindingRepositoryError,
        BundleItemWithKeyBindingRepositoryError,
        DeferredCredentialRepositoryError,
        DeleteDeferredCredentialError,
        RawCredentialDataRepositoryError
}

sealed interface CredentialClaimDisplayRepositoryError
sealed interface CredentialClaimRepositoryError
sealed interface CredentialIssuerDisplayRepositoryError
sealed interface CredentialRepositoryError
sealed interface BundleItemRepositoryError
sealed interface VerifiableCredentialRepositoryError
sealed interface CredentialWithDisplaysRepositoryError
sealed interface CredentialOfferRepositoryError
sealed interface DeleteCredentialError
sealed interface DeleteBundleItemError
sealed interface MapToCredentialClaimDataError
sealed interface GetCredentialDetailFlowError
sealed interface GetCredentialsWithDetailsFlowError
sealed interface CredentialWithKeyBindingRepositoryError
sealed interface BundleItemWithKeyBindingRepositoryError
sealed interface DeferredCredentialRepositoryError
sealed interface DeleteDeferredCredentialError
sealed interface RawCredentialDataRepositoryError

internal fun BundleItemRepositoryError.toRefreshBatchCredentialsError(): RefreshBatchCredentialsError = when (this) {
    is SsiError.Unexpected -> RefreshBatchCredentialsError.Unexpected(cause)
}

internal fun BundleItemRepositoryError.toMapToCredentialDisplayDataError(): MapToCredentialDisplayDataError = when (this) {
    is SsiError.Unexpected -> CredentialError.Unexpected(cause)
}

internal fun CredentialRepositoryError.toRefreshBatchCredentialsError(): RefreshBatchCredentialsError = when (this) {
    is SsiError.Unexpected -> RefreshBatchCredentialsError.Unexpected(cause)
}

internal fun BundleItemRepositoryError.toDeleteBundleItemError() = when (this) {
    is SsiError.Unexpected -> SsiError.Unexpected(cause)
}

internal fun BundleItemWithKeyBindingRepositoryError.toDeleteBundleItemError() = when (this) {
    is SsiError.Unexpected -> SsiError.Unexpected(cause)
}

internal fun CredentialRepositoryError.toDeleteCredentialError() = when (this) {
    is SsiError.Unexpected -> SsiError.Unexpected(cause)
}
internal fun CredentialWithKeyBindingRepositoryError.toDeleteCredentialError() = when (this) {
    is SsiError.Unexpected -> SsiError.Unexpected(cause)
}

internal fun Throwable.toMapToCredentialClaimDataError(message: String): MapToCredentialClaimDataError {
    Timber.e(t = this, message = message)
    return SsiError.Unexpected(this)
}

internal fun MatchKeyBindingToPayloadCnfError.toCredentialOfferRepositoryError(): CredentialOfferRepositoryError = when (this) {
    is MatchKeyBindingToPayloadCnfError.Unexpected -> SsiError.Unexpected(throwable)
}

internal fun Throwable.toCredentialOfferRepositoryError(message: String): CredentialOfferRepositoryError {
    Timber.e(t = this, message = message)
    return SsiError.Unexpected(this)
}

internal fun Throwable.toCredentialIssuerDisplayRepositoryError(message: String): CredentialIssuerDisplayRepositoryError {
    Timber.e(t = this, message = message)
    return SsiError.Unexpected(this)
}

internal fun Throwable.toVerifiableCredentialWithDisplaysAndClustersRepositoryError(
    message: String
): CredentialWithDisplaysRepositoryError {
    Timber.e(t = this, message = message)
    return SsiError.Unexpected(this)
}

internal fun MapToCredentialClaimDataError.toGetCredentialDetailFlowError(): GetCredentialDetailFlowError = when (this) {
    is SsiError.Unexpected -> this
}

internal fun CredentialWithDisplaysRepositoryError.toGetCredentialDetailFlowError():
    GetCredentialDetailFlowError = when (this) {
    is SsiError.Unexpected -> this
}

internal fun CredentialWithDisplaysRepositoryError.toGetCredentialsWithDetailsFlowError():
    GetCredentialsWithDetailsFlowError = when (this) {
    is SsiError.Unexpected -> this
}

internal fun MapToCredentialDisplayDataError.toGetCredentialDetailFlowError(): GetCredentialDetailFlowError = when (this) {
    is CredentialError.Unexpected -> SsiError.Unexpected(cause)
}

internal fun MapToCredentialDisplayDataError.toGetCredentialsWithDisplaysFlowError(): GetCredentialsWithDetailsFlowError = when (this) {
    is CredentialError.Unexpected -> SsiError.Unexpected(cause)
}

internal fun DeferredCredentialRepositoryError.toDeleteDeferredCredentialError() = when (this) {
    is SsiError.Unexpected -> SsiError.Unexpected(cause)
}

internal fun CredentialRepositoryError.toDeleteDeferredCredentialError2() = when (this) {
    is SsiError.Unexpected -> SsiError.Unexpected(cause)
}
