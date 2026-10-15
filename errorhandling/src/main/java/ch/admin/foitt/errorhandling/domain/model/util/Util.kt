package ch.admin.foitt.errorhandling.domain.model.util

import ch.admin.foitt.errorhandling.domain.model.AppError
import ch.admin.foitt.errorhandling.domain.model.ErrorOccurrence
import ch.admin.foitt.errorhandling.domain.model.LogLevel
import timber.log.Timber

fun AppError.log(prefix: String) {
    val errorCode = "$prefix-${errorOccurrence.errorCode.value}"
    when (errorOccurrence.logLevel) {
        LogLevel.NONE -> {}
        LogLevel.DEBUG -> Timber.d(t = errorOccurrence.throwable, message = errorOccurrence.createMessage(errorCode))
        LogLevel.DYNATRACE -> Timber.e(t = errorOccurrence.throwable, message = errorOccurrence.createMessage(errorCode))
    }
}

private fun ErrorOccurrence.createMessage(errorCode: String) = buildString {
    append("errorCode: $errorCode")
    append(", details: ")
    details?.let {
        append(it)
    } ?: append("N/A")
    append(", steps: ${steps.joinToString(prefix = "[", postfix = "]", separator = ", ")}")
}
