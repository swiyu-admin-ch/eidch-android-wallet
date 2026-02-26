@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.oca.domain.model

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.FetchTypeMetadataError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.FetchVcSchemaError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.TypeMetadataError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSchemaError
import ch.admin.foitt.openid4vc.utils.SafeGetError
import ch.admin.foitt.openid4vc.utils.SafeGetUrlError
import ch.admin.foitt.sriValidator.domain.model.SRIError
import ch.admin.foitt.wallet.platform.jsonSchema.domain.model.JsonSchemaError
import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import timber.log.Timber
import java.io.IOException

sealed interface OcaError {
    data object InvalidOca : FetchVcMetadataByFormatError, FetchOcaBundleError, OcaBundlerError
    data object InvalidJsonScheme : FetchVcMetadataByFormatError
    data object NetworkError :
        OcaRepositoryError,
        FetchVcMetadataByFormatError,
        FetchOcaBundleError

    data object InvalidCaptureBases : OcaBundlerError
    data object InvalidOverlays : OcaBundlerError
    data object InvalidJsonObject : OcaBundlerError

    data object InvalidRootCaptureBase : OcaCaptureBaseValidationError, GetRootCaptureBaseError, GenerateOcaDisplaysError
    data object InvalidCaptureBaseReferenceAttribute : OcaCaptureBaseValidationError
    data object CaptureBaseCycleError : OcaCaptureBaseValidationError

    data object MissingMandatoryOverlay : OcaOverlayValidationError
    data object InvalidOverlayCaptureBaseDigest : OcaOverlayValidationError
    data object InvalidOverlayLanguageCode : OcaOverlayValidationError
    data object InvalidDataSourceOverlay : OcaOverlayValidationError
    data object InvalidBrandingOverlay : OcaOverlayValidationError
    data object InvalidClusterOverlay : OcaOverlayValidationError
    data object InvalidEntryCodeOverlay : OcaOverlayValidationError
    data object InvalidEntryOverlay : OcaOverlayValidationError
    data object UnsupportedCredentialFormat : FetchVcMetadataByFormatError

    data class InvalidCESRHash(val msg: String) : OcaCesrHashValidatorError, OcaBundlerError

    data class Unexpected(val cause: Throwable?) :
        OcaRepositoryError,
        OcaCesrHashValidatorError,
        FetchOcaBundleError,
        FetchVcMetadataByFormatError,
        OcaBundlerError,
        GenerateOcaDisplaysError
}

sealed interface OcaRepositoryError
sealed interface FetchVcMetadataByFormatError
sealed interface FetchOcaBundleError
sealed interface OcaCesrHashValidatorError
sealed interface OcaBundlerError
sealed interface OcaCaptureBaseValidationError
sealed interface OcaOverlayValidationError
sealed interface GenerateOcaDisplaysError
sealed interface GetRootCaptureBaseError

fun Throwable.toOcaRepositoryError(message: String): OcaRepositoryError {
    Timber.e(t = this, message = message)
    return when (this) {
        is IOException -> OcaError.NetworkError
        else -> OcaError.Unexpected(this)
    }
}

fun SafeGetUrlError.toFetchOcaBundleError(): FetchOcaBundleError = when (this) {
    is SafeGetError.Unexpected -> OcaError.InvalidOca
}

fun OcaRepositoryError.toFetchOcaBundleError(): FetchOcaBundleError = when (this) {
    is OcaError.Unexpected -> this
    is OcaError.NetworkError -> this
}

fun SRIError.toFetchOcaBundleError(): FetchOcaBundleError = when (this) {
    is SRIError.UnsupportedAlgorithm,
    is SRIError.MalformedIntegrity,
    is SRIError.ValidationFailed -> OcaError.InvalidOca
}

fun FetchTypeMetadataError.toFetchVcMetadataByFormatError(): FetchVcMetadataByFormatError = when (this) {
    is TypeMetadataError.InvalidData -> OcaError.InvalidOca
    is TypeMetadataError.NetworkError -> OcaError.NetworkError
    is TypeMetadataError.Unexpected -> OcaError.Unexpected(cause)
}

internal fun JsonParsingError.toCesrHashValidatorError(): OcaCesrHashValidatorError = when (this) {
    is JsonError.Unexpected -> OcaError.Unexpected(throwable)
}

fun SafeGetUrlError.toFetchVcMetadataByFormatError(): FetchVcMetadataByFormatError = when (this) {
    SafeGetError.Unexpected -> OcaError.InvalidOca
}

fun FetchVcSchemaError.toFetchVcMetadataByFormatError(): FetchVcMetadataByFormatError = when (this) {
    is VcSchemaError.InvalidVcSchema -> OcaError.InvalidOca
    is VcSchemaError.NetworkError -> OcaError.NetworkError
    is VcSchemaError.Unexpected -> OcaError.Unexpected(cause)
}

fun FetchOcaBundleError.toFetchVcMetadataByFormatError(): FetchVcMetadataByFormatError = when (this) {
    is OcaError.InvalidOca -> this
    is OcaError.NetworkError -> this
    is OcaError.Unexpected -> this
}

fun JsonParsingError.toOcaBundlerError(): OcaBundlerError = when (this) {
    is JsonError.Unexpected -> OcaError.InvalidJsonObject
}

fun OcaCesrHashValidatorError.toOcaBundlerError(): OcaBundlerError = when (this) {
    is OcaError.InvalidCESRHash -> this
    is OcaError.Unexpected -> this
}

fun OcaCaptureBaseValidationError.toOcaBundlerError(): OcaBundlerError = when (this) {
    is OcaError.InvalidRootCaptureBase,
    is OcaError.InvalidCaptureBaseReferenceAttribute,
    is OcaError.CaptureBaseCycleError -> OcaError.InvalidCaptureBases
}

fun OcaOverlayValidationError.toOcaBundlerError(): OcaBundlerError = when (this) {
    is OcaError.MissingMandatoryOverlay,
    is OcaError.InvalidOverlayCaptureBaseDigest,
    is OcaError.InvalidDataSourceOverlay,
    is OcaError.InvalidBrandingOverlay,
    is OcaError.InvalidClusterOverlay,
    is OcaError.InvalidEntryCodeOverlay,
    is OcaError.InvalidEntryOverlay,
    is OcaError.InvalidOverlayLanguageCode -> OcaError.InvalidOverlays
}

fun GetRootCaptureBaseError.toOcaBundlerError(): OcaBundlerError = when (this) {
    is OcaError.InvalidRootCaptureBase -> OcaError.InvalidCaptureBases
}

fun JsonSchemaError.toFetchVcMetadataByFormatError(): FetchVcMetadataByFormatError = when (this) {
    JsonSchemaError.ValidationFailed -> OcaError.InvalidJsonScheme
}

fun GetRootCaptureBaseError.toOcaCaptureBaseValidationError(): OcaCaptureBaseValidationError = when (this) {
    is OcaError.InvalidRootCaptureBase -> this
}

fun GetRootCaptureBaseError.toGenerateOcaDisplaysError(): GenerateOcaDisplaysError = when (this) {
    is OcaError.InvalidRootCaptureBase -> this
}
