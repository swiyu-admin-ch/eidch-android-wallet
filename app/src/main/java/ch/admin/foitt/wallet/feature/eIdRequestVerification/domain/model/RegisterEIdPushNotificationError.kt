package ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model

import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.RegisterEIdPushNotificationError.InvalidClientAttestation
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.RegisterEIdPushNotificationError.InvalidFormat
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.RegisterEIdPushNotificationError.NetworkError
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.RegisterEIdPushNotificationError.Unexpected
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SetEIdPeerPushIdError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.FetchPushDeviceTokenError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.GeneratePushClientAttestationError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.PushNotificationError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.RegisterPushDeviceTokenError
import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.JsonParsingError

sealed interface RegisterEIdPushNotificationError {
    data object InvalidFormat : RegisterEIdPushNotificationError

    data object NetworkError : RegisterEIdPushNotificationError

    data object InvalidClientAttestation : RegisterEIdPushNotificationError

    data class Unexpected(val cause: Throwable?) : RegisterEIdPushNotificationError
}

internal fun EIdRequestCaseRepositoryError.toRegisterEIdPushNotificationError(): RegisterEIdPushNotificationError = when (this) {
    is EIdRequestError.Unexpected -> Unexpected(cause)
}

internal fun SetEIdPeerPushIdError.toRegisterEIdPushNotificationError(): RegisterEIdPushNotificationError = when (this) {
    EIdRequestError.NetworkError -> NetworkError
    is EIdRequestError.Unexpected -> Unexpected(cause)
}

internal fun FetchPushDeviceTokenError.toRegisterEIdPushNotificationError(): RegisterEIdPushNotificationError = when (this) {
    is PushNotificationError.Unexpected -> Unexpected(cause)
}

internal fun GeneratePushClientAttestationError.toRegisterEIdPushNotificationError(): RegisterEIdPushNotificationError = when (this) {
    PushNotificationError.InvalidClientAttestation -> InvalidClientAttestation
    PushNotificationError.NetworkError -> NetworkError
    is PushNotificationError.Unexpected -> Unexpected(cause)
}

internal fun RegisterPushDeviceTokenError.toRegisterEIdPushNotificationError(): RegisterEIdPushNotificationError = when (this) {
    PushNotificationError.InvalidClientAttestation -> InvalidClientAttestation
    PushNotificationError.InvalidFormat -> InvalidFormat
    PushNotificationError.NetworkError -> NetworkError
    is PushNotificationError.Unexpected -> Unexpected(cause)
}

internal fun JsonParsingError.toRegisterEIdPushNotificationError(): RegisterEIdPushNotificationError = when (this) {
    is JsonError.Unexpected -> Unexpected(throwable)
}
