@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.nonCompliance.domain.model

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityListError
import ch.admin.foitt.wallet.platform.activityList.domain.model.CredentialActivityRepositoryError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.GenerateProofOfPossessionError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.RequestClientAttestationError
import ch.admin.foitt.wallet.platform.ssi.domain.model.BundleItemRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.RawCredentialDataRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.model.VerifiableCredentialRepositoryError
import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import timber.log.Timber

sealed interface NonComplianceError {
    data object InvalidClientAttestation : SendNonComplianceReportError
    data object NetworkError : SendNonComplianceReportError
    data class Unexpected(val throwable: Throwable?) :
        NonComplianceRepositoryError,
        SendNonComplianceReportError,
        GetNonComplianceReportingDataError
}

sealed interface NonComplianceRepositoryError
sealed interface SendNonComplianceReportError
sealed interface GetNonComplianceReportingDataError

fun Throwable.toNonComplianceRepositoryError(message: String): NonComplianceRepositoryError {
    Timber.e(t = this, message = message)
    return NonComplianceError.Unexpected(this)
}

fun JsonParsingError.toNonComplianceRepositoryError(): NonComplianceRepositoryError = when (this) {
    is JsonError.Unexpected -> NonComplianceError.Unexpected(throwable)
}

fun NonComplianceRepositoryError.toSendNonComplianceReportError(): SendNonComplianceReportError = when (this) {
    is NonComplianceError.Unexpected -> this
}

fun JsonParsingError.toSendNonComplianceReportError(): SendNonComplianceReportError = when (this) {
    is JsonError.Unexpected -> NonComplianceError.Unexpected(throwable)
}

internal fun RequestClientAttestationError.toSendNonComplianceReportError(): SendNonComplianceReportError = when (this) {
    is AttestationError.ValidationError -> NonComplianceError.InvalidClientAttestation
    is AttestationError.NetworkError -> NonComplianceError.NetworkError
    AttestationError.SocketTimeoutError -> NonComplianceError.Unexpected(null)
    is AttestationError.Unexpected -> NonComplianceError.Unexpected(throwable)
}

internal fun GenerateProofOfPossessionError.toSendNonComplianceReportError(): SendNonComplianceReportError = when (this) {
    is AttestationError.Unexpected -> NonComplianceError.Unexpected(throwable)
}

fun CredentialActivityRepositoryError.toSendNonComplianceReportError(): SendNonComplianceReportError = when (this) {
    is ActivityListError.Unexpected -> NonComplianceError.Unexpected(throwable)
}

fun VerifiableCredentialRepositoryError.toSendNonComplianceReportError(): SendNonComplianceReportError = when (this) {
    is SsiError.Unexpected -> NonComplianceError.Unexpected(cause)
}
internal fun RawCredentialDataRepositoryError.toSendNonComplianceReportError(): SendNonComplianceReportError = when (this) {
    is SsiError.Unexpected -> NonComplianceError.Unexpected(cause)
}

internal fun BundleItemRepositoryError.toSendNonComplianceReportError(): SendNonComplianceReportError = when (this) {
    is SsiError.Unexpected -> NonComplianceError.Unexpected(cause)
}

internal fun CredentialRepositoryError.toSendNonComplianceReportError(): SendNonComplianceReportError = when (this) {
    is SsiError.Unexpected -> NonComplianceError.Unexpected(cause)
}
