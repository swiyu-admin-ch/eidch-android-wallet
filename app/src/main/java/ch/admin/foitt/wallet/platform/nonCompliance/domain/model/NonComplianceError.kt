package ch.admin.foitt.wallet.platform.nonCompliance.domain.model

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityListError
import ch.admin.foitt.wallet.platform.activityList.domain.model.CredentialActivityRepositoryError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.GenerateProofOfPossessionError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.RequestClientAttestationError
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
        FetchNonComplianceDataError,
        SendNonComplianceReportError
}

sealed interface FetchNonComplianceDataError
sealed interface NonComplianceRepositoryError
sealed interface SendNonComplianceReportError

fun Throwable.toNonComplianceRepositoryError(message: String): NonComplianceRepositoryError {
    Timber.e(t = this, message = message)
    return NonComplianceError.Unexpected(this)
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
