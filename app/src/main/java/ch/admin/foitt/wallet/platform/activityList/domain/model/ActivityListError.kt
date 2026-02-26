package ch.admin.foitt.wallet.platform.activityList.domain.model

import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.MapToCredentialDisplayDataError
import timber.log.Timber

interface ActivityListError {
    data class Unexpected(val throwable: Throwable?) :
        CredentialActivityRepositoryError,
        ActivityClaimRepositoryError,
        ActivityActorDisplayRepositoryError,
        ActivityWithDetailsRepositoryError,
        ActivityWithActorDisplaysRepositoryError,
        ActivityActorDisplayWithImageRepositoryError,
        GetActivityActorDisplaysWithImagesFlowError,
        ImageRepositoryError,
        ActivityRepositoryError,
        MapToActivityDisplayDataError,
        GetActivitiesWithDisplaysFlowError,
        GetActivityDetailFlowError,
        GetActivityWithDetailsFlowError,
        DeleteActivityError
}

sealed interface CredentialActivityRepositoryError
sealed interface ActivityClaimRepositoryError
sealed interface ActivityActorDisplayRepositoryError
sealed interface ActivityWithDetailsRepositoryError
sealed interface ActivityWithActorDisplaysRepositoryError
sealed interface ActivityActorDisplayWithImageRepositoryError
sealed interface ImageRepositoryError
sealed interface ActivityRepositoryError
sealed interface MapToActivityDisplayDataError
sealed interface GetActivitiesWithDisplaysFlowError
sealed interface GetActivityActorDisplaysWithImagesFlowError
sealed interface GetActivityDetailFlowError
sealed interface GetActivityWithDetailsFlowError
sealed interface DeleteActivityError

fun Throwable.toActivityWithDetailsRepositoryError(message: String): ActivityWithDetailsRepositoryError {
    Timber.e(t = this, message = message)
    return ActivityListError.Unexpected(this)
}

fun Throwable.toActivityWithDisplaysRepositoryError(message: String): ActivityWithActorDisplaysRepositoryError {
    Timber.e(t = this, message = message)
    return ActivityListError.Unexpected(this)
}

fun Throwable.toActivityRepositoryError(message: String): ActivityRepositoryError {
    Timber.e(t = this, message = message)
    return ActivityListError.Unexpected(this)
}

fun Throwable.toActivityActorDisplayWithImageRepositoryError(message: String): ActivityActorDisplayWithImageRepositoryError {
    Timber.e(t = this, message = message)
    return ActivityListError.Unexpected(this)
}

fun ActivityWithActorDisplaysRepositoryError.toGetActivitiesWithDisplaysFlowError(): GetActivitiesWithDisplaysFlowError = when (this) {
    is ActivityListError.Unexpected -> this
}

internal fun MapToCredentialDisplayDataError.toGetActivityDetailFlowError(): GetActivityDetailFlowError = when (this) {
    is CredentialError.Unexpected -> ActivityListError.Unexpected(cause)
}

fun CredentialActivityRepositoryError.toDeleteActivityError(): DeleteActivityError = when (this) {
    is ActivityListError.Unexpected -> this
}

fun ActivityActorDisplayWithImageRepositoryError.toGetActivityActorDisplaysWithImagesFlowError():
    GetActivityActorDisplaysWithImagesFlowError = when (this) {
    is ActivityListError.Unexpected -> this
}

fun ActivityWithDetailsRepositoryError.toGetActivityWithDetailsFlowError(): GetActivityWithDetailsFlowError = when (this) {
    is ActivityListError.Unexpected -> this
}
