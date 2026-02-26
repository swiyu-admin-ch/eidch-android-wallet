package ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.LocalizedDisplay

// Test data
internal data class MockLocalizedDisplay(
    override val locale: String
) : LocalizedDisplay

internal object LocalizedDisplayTestData {
    val withSupportedLocaleWithCountryCode = listOf("en", "fr", "de-CH", "fallback").map {
        MockLocalizedDisplay(locale = it)
    }
    val withSupportedLocaleWithDifferentCountryCode = listOf("en", "fr", "de-DE", "fallback").map {
        MockLocalizedDisplay(locale = it)
    }
    val withSupportedLocaleNoCountryCode = listOf("en", "it", "fallback", "yy").map { MockLocalizedDisplay(locale = it) }
    val noSupportedLocaleAndNoFallback = listOf("xx", "yy").map { MockLocalizedDisplay(locale = it) }
    val withFallbackAndNoSupportedLocale = listOf("yy", "fallback").map { MockLocalizedDisplay(locale = it) }
}

internal object LocalizedAndThemedDisplayTestData {
    val withSupportedLocaleWithCountryCode = listOf("en", "fr", "de-CH", "fallback").map {
        createCredentialDisplay(it)
    }

    val withSupportedLocaleWithDifferentCountryCode = listOf("en", "fr", "de-DE", "fallback").map {
        createCredentialDisplay(it)
    }
    val withSupportedLocaleNoCountryCode = listOf("en", "it", "fallback", "yy").map {
        createCredentialDisplay(it)
    }
    val noSupportedLocaleAndNoFallback = listOf("xx", "yy").map {
        createCredentialDisplay(it)
    }
    val withFallbackAndNoSupportedLocale = listOf("yy", "fallback").map {
        createCredentialDisplay(it)
    }
}

private fun createCredentialDisplay(locale: String) = CredentialDisplay(
    credentialId = 1,
    locale = locale,
    theme = "dark"
)
