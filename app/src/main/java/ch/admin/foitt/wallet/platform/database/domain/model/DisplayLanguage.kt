package ch.admin.foitt.wallet.platform.database.domain.model

import ch.admin.foitt.wallet.platform.locale.LocaleCompat

object DisplayLanguage {
    const val DEFAULT = "en"
    const val DEFAULT_COUNTRY = "CH"
    const val FALLBACK = "fallback"
    const val UNKNOWN = "unknown"
    val PRIORITIES = listOf(
        LocaleCompat.of("de"),
        LocaleCompat.of("en"),
        LocaleCompat.ofUnknownFormat(UNKNOWN),
        LocaleCompat.ofUnknownFormat(FALLBACK)
    )
}
