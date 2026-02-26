package ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.OidIssuerDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.locale.LocaleCompat
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedCredentialInformationDisplay
import java.util.Locale
import javax.inject.Inject

class GetLocalizedCredentialInformationDisplayImpl @Inject constructor(
    private val getCurrentAppLocale: GetCurrentAppLocale
) : GetLocalizedCredentialInformationDisplay {

    override fun invoke(
        displays: List<OidIssuerDisplay>,
        preferredLocaleString: String?,
    ): OidIssuerDisplay? {
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
    private fun List<OidIssuerDisplay>.getWithLanguageAndCountry(
        preferredLocale: Locale,
    ) = firstOrNull { display ->
        display.locale?.let {
            LocaleCompat.ofUnknownFormat(it) == preferredLocale
        } ?: false
    }

    /**
     * Looks for display containing a language-(anyCountry)
     * Ex: preferred locale: "fr-CH"
     * Display with "fr-(anyCountry)" -> return this match
     * No display with "fr-(anyCountry)" -> null
     */
    private fun List<OidIssuerDisplay>.getWithLanguageAndDifferentCountry(
        preferredLocale: Locale,
    ) = firstOrNull { display ->
        display.locale?.let {
            val displayLocale = LocaleCompat.ofUnknownFormat(it)
            displayLocale.language == preferredLocale.language && displayLocale.country != null
        } ?: false
    }

    /**
     * Looks for display containing a language
     * Ex: preferred locale: "fr-CH"
     * Display with "fr" -> return this match
     * No display with "fr" -> null
     */
    private fun List<OidIssuerDisplay>.getWithLanguage(
        preferredLocale: Locale,
    ) = firstOrNull { display ->
        display.locale?.let {
            val displayLocale = LocaleCompat.ofUnknownFormat(it)
            displayLocale.language == preferredLocale.language
        } ?: false
    }

    /**
     * Looks for display containing a (prioritized) default language
     * Ex: preferred locale: "fr-CH"
     * Display with "de" -> return this match
     * No display with match in prio list -> null
     */
    private fun List<OidIssuerDisplay>.getWithDefault(): OidIssuerDisplay? {
        // Create a map of preferred languages
        // The map value indicates the preference order (lower index => higher priority)
        val prioritizedLocales: Map<Locale, Int> = DisplayLanguage.PRIORITIES
            .withIndex()
            .associate { it.value to it.index }

        return this.minByOrNull { display ->
            display.locale?.let {
                prioritizedLocales[LocaleCompat.ofUnknownFormat(it)] ?: Int.MAX_VALUE
            } ?: Int.MAX_VALUE
        }
    }
}
