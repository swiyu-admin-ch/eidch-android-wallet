package ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.locale.LocaleCompat
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedAndThemedDisplay
import ch.admin.foitt.wallet.platform.theme.domain.model.Theme
import java.util.Locale
import javax.inject.Inject

class GetLocalizedAndThemedDisplayImpl @Inject constructor(
    private val getCurrentAppLocale: GetCurrentAppLocale,
) : GetLocalizedAndThemedDisplay {
    override fun invoke(
        credentialDisplays: List<CredentialDisplay>,
        preferredLocaleString: String?,
        preferredTheme: Theme,
    ): CredentialDisplay? {
        val appLocale = getCurrentAppLocale()
        val theme = preferredTheme.value

        val preferredLocale = preferredLocaleString?.let {
            LocaleCompat.ofUnknownFormat(it)
        } ?: appLocale

        // Preferred Locale in this example is "fr-CH", theme is "dark"
        // Search for Display with a perfect match, f. ex. "fr-CH" and "dark" (might return "light" if "dark" not found)
        return credentialDisplays.getWithLocaleAndTheme(preferredLocale = preferredLocale, theme = theme)
            // Next: language match, but different country, f. ex. "fr-FR" (might return "light" if "dark" not found)
            ?: credentialDisplays.getWithLanguageAndDifferentCountryAndTheme(preferredLocale = preferredLocale, theme = theme)
            // Next: language match, country not available, f. ex. "fr" (might return "light" if "dark" not found)
            ?: credentialDisplays.getWithLanguageAndTheme(preferredLocale = preferredLocale, theme = theme)
            // Next: check for default languages, f. e "en" etc. (see prioritized list) (might return "light" if "dark" not found)
            ?: credentialDisplays.getWithDefaultAndTheme(theme = theme)
            // Last resort: take first if available
            ?: credentialDisplays.firstOrNull()
    }

    /**
     * Looks for display containing a complete locale and theme match
     * Ex: preferred locale: "fr-CH" and theme: "dark"
     * Display with "fr-CH" and theme "dark" -> return this perfect match
     * Display with "fr-CH" but theme "light" -> return this one as at least the locale matches
     * No display with "fr-CH" -> null
     */
    private fun List<CredentialDisplay>.getWithLocaleAndTheme(
        preferredLocale: Locale,
        theme: String,
    ): CredentialDisplay? {
        val displays = this.filter { LocaleCompat.ofUnknownFormat(it.locale) == preferredLocale }

        return displays.firstOrNull { it.theme == theme } ?: displays.firstOrNull()
    }

    /**
     * Looks for display containing a language-(anyCountry) and theme match
     * Ex: preferred locale: "fr-CH" and theme: "dark"
     * Display with "fr-(anyCountry)" and theme "dark" -> return this match
     * Display with "fr-(anyCountry)" but theme "light" -> return this one as at least the language matches
     * No display with "fr-(anyCountry)" -> null
     */
    private fun List<CredentialDisplay>.getWithLanguageAndDifferentCountryAndTheme(
        preferredLocale: Locale,
        theme: String,
    ): CredentialDisplay? {
        val displays = this.filter { display ->
            val displayLocale = LocaleCompat.ofUnknownFormat(display.locale)
            displayLocale.language == preferredLocale.language && displayLocale.country != null
        }

        return displays.firstOrNull { it.theme == theme } ?: displays.firstOrNull()
    }

    /**
     * Looks for display containing a language and theme match
     * Ex: preferred locale: "fr-CH" and theme: "dark"
     * Display with "fr" and theme "dark" -> return this match
     * Display with "fr" but theme "light" -> return this one as at least the language matches
     * No display with "fr" -> null
     */
    private fun List<CredentialDisplay>.getWithLanguageAndTheme(
        preferredLocale: Locale,
        theme: String,
    ): CredentialDisplay? {
        val displays = this.filter { display ->
            LocaleCompat.ofUnknownFormat(display.locale).language == preferredLocale.language
        }

        return displays.firstOrNull { it.theme == theme } ?: displays.firstOrNull()
    }

    /**
     * Looks for display containing a (prioritized) default language and theme match
     * Ex: preferred locale: "fr-CH" and theme: "dark"
     * Display with "de" and theme "dark" -> return this match
     * Display with "de" but theme "light" -> return this one as at least the language matches the highest fallback prio
     * No display with match in prio list -> null
     */
    private fun List<CredentialDisplay>.getWithDefaultAndTheme(
        theme: String,
    ): CredentialDisplay? {
        // Create a map of preferred languages
        // The map value indicates the preference order (lower index => higher priority)
        val prioritizedLocales: Map<Locale, Int> = DisplayLanguage.PRIORITIES
            .withIndex()
            .associate { it.value to it.index }

        val bestMatchingLocaleString = this.minByOrNull { display ->
            prioritizedLocales[LocaleCompat.ofUnknownFormat(display.locale)] ?: Int.MAX_VALUE
        }?.locale
        val bestMatchingLocale = bestMatchingLocaleString?.let {
            LocaleCompat.ofUnknownFormat(it)
        }

        val displaysWithBestMatchingLocale = this.filter { display ->
            LocaleCompat.ofUnknownFormat(display.locale).toLanguageTag() == bestMatchingLocale?.toLanguageTag()
        }

        return displaysWithBestMatchingLocale.firstOrNull { it.theme == theme } ?: displaysWithBestMatchingLocale.firstOrNull()
    }
}
