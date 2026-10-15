package ch.admin.foitt.errorhandling.domain.model

import java.time.Instant

/**
 * A single step in the error's mapping journey through layers and scopes.
 */
data class Step(
    val description: String,
    val createdAt: Instant = Instant.now()
)
