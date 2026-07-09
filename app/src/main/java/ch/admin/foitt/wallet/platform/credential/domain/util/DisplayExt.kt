package ch.admin.foitt.wallet.platform.credential.domain.util

import ch.admin.foitt.wallet.platform.credential.domain.model.AnyDisplay

fun <T : AnyDisplay> List<T>?.addFallbackLanguage(
    fallbackValue: () -> T
): List<T> {
    if (this == null) {
        return listOf(fallbackValue())
    }
    return this + fallbackValue()
}
