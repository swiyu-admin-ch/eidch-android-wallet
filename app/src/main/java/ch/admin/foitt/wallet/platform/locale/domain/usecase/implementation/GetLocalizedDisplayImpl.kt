package ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.database.domain.model.LocalizedDisplay
import ch.admin.foitt.wallet.platform.locale.LocaleCompat
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import java.util.Locale
import javax.inject.Inject

class GetLocalizedDisplayImpl @Inject constructor(
    private val getCurrentAppLocale: GetCurrentAppLocale
) : GetLocalizedDisplay {

    override fun <T : LocalizedDisplay> invoke(
        displays: Collection<T>,
        preferredLocaleString: String?,
    ): T? {
        val appLocale = getCurrentAppLocale()

        val preferredLocale = preferredLocaleString?.let {
            LocaleCompat.ofUnknownFormat(it)
        } ?: appLocale

        // Preferred Locale in this example is "fr-CH"
        // Search for LocalizedDisplay with a perfect match, f. ex. "fr-CH"
        return displays.getWithLanguageAndCountry(preferredLocale = preferredLocale)
            // Next: language match, but different country, f. ex. "fr-FR"
            ?: displays.getWithLanguageAndDifferentCountry(preferredLocale = preferredLocale)
            // Next: language match, country not available, f. ex. "fr"
            ?: displays.getWithLanguage(preferredLocale = preferredLocale)
            // Next: check for default languages, f. e "en" etc. (see prioritized list)
            ?: displays.getWithDefault()
            // Last resort: take first if available
            ?: displays.firstOrNull()
    }

    /**
     * Looks for display containing a complete locale
     * Ex: preferred locale: "fr-CH"
     * Display with "fr-CH" -> return this perfect match
     * No display with "fr-CH" -> null
     */
    private fun <T : LocalizedDisplay> Collection<T>.getWithLanguageAndCountry(
        preferredLocale: Locale,
    ) = firstOrNull { display ->
        LocaleCompat.ofUnknownFormat(display.locale) == preferredLocale
    }

    /**
     * Looks for display containing a language-(anyCountry)
     * Ex: preferred locale: "fr-CH"
     * Display with "fr-(anyCountry)" -> return this match
     * No display with "fr-(anyCountry)" -> null
     */
    private fun <T : LocalizedDisplay> Collection<T>.getWithLanguageAndDifferentCountry(
        preferredLocale: Locale,
    ) = firstOrNull { display ->
        val displayLocale = LocaleCompat.ofUnknownFormat(display.locale)

        displayLocale.language == preferredLocale.language && displayLocale.country != null
    }

    /**
     * Looks for display containing a language
     * Ex: preferred locale: "fr-CH"
     * Display with "fr" -> return this match
     * No display with "fr" -> null
     */
    private fun <T : LocalizedDisplay> Collection<T>.getWithLanguage(
        preferredLocale: Locale,
    ) = firstOrNull { display ->
        val displayLocale = LocaleCompat.ofUnknownFormat(display.locale)

        displayLocale.language == preferredLocale.language
    }

    /**
     * Looks for display containing a (prioritized) default language
     * Ex: preferred locale: "fr-CH"
     * Display with "de" -> return this match
     * No display with match in prio list -> null
     */
    private fun <T : LocalizedDisplay> Collection<T>.getWithDefault(): T? {
        // Create a map of preferred languages
        // The map value indicates the preference order (lower index => higher priority)
        val prioritizedLocales: Map<Locale, Int> = DisplayLanguage.PRIORITIES
            .withIndex()
            .associate { it.value to it.index }

        return this.minByOrNull { display ->
            prioritizedLocales[LocaleCompat.ofUnknownFormat(display.locale)] ?: Int.MAX_VALUE
        }
    }
}
