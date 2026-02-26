package ch.admin.foitt.wallet.platform.locale

import android.os.Build
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import java.util.Locale

object LocaleCompat {
    private const val X_UNKNOWN = "x-unknown"
    private const val X_FALLBACK = "x-fallback"

    fun ofUnknownFormat(unknownFormat: String): Locale {
        val normalizedLocale = unknownFormat.replace("_", "-")
        val cleanedLocale = normalizedLocale
            .replace(DisplayLanguage.UNKNOWN, X_UNKNOWN)
            .replace(DisplayLanguage.FALLBACK, X_FALLBACK)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            Locale.forLanguageTag(cleanedLocale)
        } else {
            Locale.Builder().setLanguageTag(cleanedLocale).build()
        }
    }

    fun of(language: String): Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
        Locale.of(language)
    } else {
        Locale.Builder().setLanguage(language).build()
    }

    fun of(language: String, country: String): Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
        Locale.of(language, country)
    } else {
        Locale.Builder().setLanguage(language).setRegion(country).build()
    }
}
