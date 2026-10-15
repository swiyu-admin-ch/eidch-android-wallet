package ch.admin.foitt.errorhandling.domain.model

import java.time.Instant

/**
 * Error occurrence. Created when the original error occurs.
 */
data class ErrorOccurrence(
    val errorCode: ErrorCode,
    val logLevel: LogLevel, // if/where to log this Trace
    val throwable: Throwable? = null, // root cause of the error
    val details: String? = null, // e.g. throwable.message or s.th. custom
    val steps: List<Step> = emptyList(),
    val createdAt: Instant = Instant.now()
)
