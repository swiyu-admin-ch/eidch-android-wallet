package ch.admin.foitt.errorhandling.domain.model

/**
 * Base interface for all errors.
 */
interface AppError {
    val errorOccurrence: ErrorOccurrence
}

/**
 * Generic mapper from one AppError implementation to another.
 * Carries original error occurrence and automatically adds this mapping step to [ErrorOccurrence.steps]
 *
 * Supports bare constructor references: `oldError.mapTo(ErrorInterface2::SpecificError)`.
 */
inline fun <reified T : AppError> AppError.mapTo(factory: (ErrorOccurrence) -> T): T {
    val step = Step(description = "${this::class.simpleName} -> ${T::class.simpleName}")
    return factory(errorOccurrence.copy(steps = errorOccurrence.steps + step))
}
