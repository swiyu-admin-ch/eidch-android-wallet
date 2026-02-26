package ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.locale.LocaleCompat
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedAndThemedDisplay
import ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation.LocalizedAndThemedDisplayTestData.noSupportedLocaleAndNoFallback
import ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation.LocalizedAndThemedDisplayTestData.withFallbackAndNoSupportedLocale
import ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation.LocalizedAndThemedDisplayTestData.withSupportedLocaleNoCountryCode
import ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation.LocalizedAndThemedDisplayTestData.withSupportedLocaleWithCountryCode
import ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation.LocalizedAndThemedDisplayTestData.withSupportedLocaleWithDifferentCountryCode
import ch.admin.foitt.wallet.platform.theme.domain.model.Theme
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetLocalizedAndThemedDisplayImplTest {
    @MockK
    private lateinit var mockGetCurrentAppLocale: GetCurrentAppLocale

    private lateinit var getLocalizedAndThemedDisplay: GetLocalizedAndThemedDisplay

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        getLocalizedAndThemedDisplay = GetLocalizedAndThemedDisplayImpl(
            getCurrentAppLocale = mockGetCurrentAppLocale,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `App locale with supported country code returns CredentialDisplay in requested theme with best matching locale`() = runTest {
        coEvery { mockGetCurrentAppLocale() } returns LocaleCompat.of("de", "CH")

        val displayWithSupportedLocaleWithCountryCode = getLocalizedAndThemedDisplay(
            credentialDisplays = withSupportedLocaleWithCountryCode,
            preferredTheme = Theme.DARK
        )
        assertEquals("de-CH", displayWithSupportedLocaleWithCountryCode?.locale)
        assertEquals("dark", displayWithSupportedLocaleWithCountryCode?.theme)

        val displayWithSupportedLocaleWithDifferentCountryCode = getLocalizedAndThemedDisplay(
            credentialDisplays = withSupportedLocaleWithDifferentCountryCode,
            preferredTheme = Theme.DARK
        )
        assertEquals("de-DE", displayWithSupportedLocaleWithDifferentCountryCode?.locale)
        assertEquals("dark", displayWithSupportedLocaleWithDifferentCountryCode?.theme)

        val displayWithSupportedLocaleNoCountryCode = getLocalizedAndThemedDisplay(
            credentialDisplays = withSupportedLocaleNoCountryCode,
            preferredTheme = Theme.DARK
        )
        assertEquals("en", displayWithSupportedLocaleNoCountryCode?.locale)
        assertEquals("dark", displayWithSupportedLocaleNoCountryCode?.theme)

        val displayNoSupportedLocaleAndNoFallback = getLocalizedAndThemedDisplay(
            credentialDisplays = noSupportedLocaleAndNoFallback,
            preferredTheme = Theme.DARK
        )
        assertEquals("xx", displayNoSupportedLocaleAndNoFallback?.locale)
        assertEquals("dark", displayNoSupportedLocaleAndNoFallback?.theme)

        val displayWithFallbackAndNoSupportedLocale = getLocalizedAndThemedDisplay(
            credentialDisplays = withFallbackAndNoSupportedLocale,
            preferredTheme = Theme.DARK
        )
        assertEquals("fallback", displayWithFallbackAndNoSupportedLocale?.locale)
        assertEquals("dark", displayWithFallbackAndNoSupportedLocale?.theme)
    }

    @Test
    fun `App locale with supported country code returns CredentialDisplay in not requested theme with best matching locale`() = runTest {
        coEvery { mockGetCurrentAppLocale() } returns LocaleCompat.of("de", "CH")
        val displayWithSupportedLocaleWithCountryCode = getLocalizedAndThemedDisplay(
            credentialDisplays = withSupportedLocaleWithCountryCode,
            preferredTheme = Theme.LIGHT
        )
        assertEquals("de-CH", displayWithSupportedLocaleWithCountryCode?.locale)
        assertEquals("dark", displayWithSupportedLocaleWithCountryCode?.theme)

        val displayWithSupportedLocaleWithDifferentCountryCode = getLocalizedAndThemedDisplay(
            credentialDisplays = withSupportedLocaleWithDifferentCountryCode,
            preferredTheme = Theme.LIGHT
        )
        assertEquals("de-DE", displayWithSupportedLocaleWithDifferentCountryCode?.locale)
        assertEquals("dark", displayWithSupportedLocaleWithDifferentCountryCode?.theme)

        val displayWithSupportedLocaleNoCountryCode = getLocalizedAndThemedDisplay(
            credentialDisplays = withSupportedLocaleNoCountryCode,
            preferredTheme = Theme.LIGHT
        )
        assertEquals("en", displayWithSupportedLocaleNoCountryCode?.locale)
        assertEquals("dark", displayWithSupportedLocaleNoCountryCode?.theme)

        val displayNoSupportedLocaleAndNoFallback = getLocalizedAndThemedDisplay(
            credentialDisplays = noSupportedLocaleAndNoFallback,
            preferredTheme = Theme.LIGHT
        )
        assertEquals("xx", displayNoSupportedLocaleAndNoFallback?.locale)
        assertEquals("dark", displayNoSupportedLocaleAndNoFallback?.theme)

        val displayWithFallbackAndNoSupportedLocale = getLocalizedAndThemedDisplay(
            credentialDisplays = withFallbackAndNoSupportedLocale,
            preferredTheme = Theme.LIGHT
        )
        assertEquals("fallback", displayWithFallbackAndNoSupportedLocale?.locale)
        assertEquals("dark", displayWithFallbackAndNoSupportedLocale?.theme)
    }

    @Test
    fun `App locale with unsupported country code returns CredentialDisplay in requested theme with best matching locale`() = runTest {
        coEvery { mockGetCurrentAppLocale() } returns LocaleCompat.of("de", "XX")
        val displayWithSupportedLocaleWithCountryCode = getLocalizedAndThemedDisplay(
            credentialDisplays = withSupportedLocaleWithCountryCode,
            preferredTheme = Theme.DARK
        )
        assertEquals("de-CH", displayWithSupportedLocaleWithCountryCode?.locale)
        assertEquals("dark", displayWithSupportedLocaleWithCountryCode?.theme)

        val displayWithSupportedLocaleWithDifferentCountryCode = getLocalizedAndThemedDisplay(
            credentialDisplays = withSupportedLocaleWithDifferentCountryCode,
            preferredTheme = Theme.DARK
        )
        assertEquals("de-DE", displayWithSupportedLocaleWithDifferentCountryCode?.locale)
        assertEquals("dark", displayWithSupportedLocaleWithDifferentCountryCode?.theme)

        val displayWithSupportedLocaleNoCountryCode = getLocalizedAndThemedDisplay(
            credentialDisplays = withSupportedLocaleNoCountryCode,
            preferredTheme = Theme.DARK
        )
        assertEquals("en", displayWithSupportedLocaleNoCountryCode?.locale)
        assertEquals("dark", displayWithSupportedLocaleNoCountryCode?.theme)

        val displayNoSupportedLocaleAndNoFallback = getLocalizedAndThemedDisplay(
            credentialDisplays = noSupportedLocaleAndNoFallback,
            preferredTheme = Theme.DARK
        )
        assertEquals("xx", displayNoSupportedLocaleAndNoFallback?.locale)
        assertEquals("dark", displayNoSupportedLocaleAndNoFallback?.theme)

        val displayWithFallbackAndNoSupportedLocale = getLocalizedAndThemedDisplay(
            credentialDisplays = withFallbackAndNoSupportedLocale,
            preferredTheme = Theme.DARK
        )
        assertEquals("fallback", displayWithFallbackAndNoSupportedLocale?.locale)
        assertEquals("dark", displayWithFallbackAndNoSupportedLocale?.theme)
    }

    @Test
    fun `App locale with unsupported country code returns CredentialDisplay in not requested theme with best matching locale`() = runTest {
        coEvery { mockGetCurrentAppLocale() } returns LocaleCompat.of("de", "XX")
        val displayWithSupportedLocaleWithCountryCode = getLocalizedAndThemedDisplay(
            credentialDisplays = withSupportedLocaleWithCountryCode,
            preferredTheme = Theme.LIGHT
        )
        assertEquals("de-CH", displayWithSupportedLocaleWithCountryCode?.locale)
        assertEquals("dark", displayWithSupportedLocaleWithCountryCode?.theme)

        val displayWithSupportedLocaleWithDifferentCountryCode = getLocalizedAndThemedDisplay(
            credentialDisplays = withSupportedLocaleWithDifferentCountryCode,
            preferredTheme = Theme.LIGHT
        )
        assertEquals("de-DE", displayWithSupportedLocaleWithDifferentCountryCode?.locale)
        assertEquals("dark", displayWithSupportedLocaleWithDifferentCountryCode?.theme)

        val displayWithSupportedLocaleNoCountryCode = getLocalizedAndThemedDisplay(
            credentialDisplays = withSupportedLocaleNoCountryCode,
            preferredTheme = Theme.LIGHT
        )
        assertEquals("en", displayWithSupportedLocaleNoCountryCode?.locale)
        assertEquals("dark", displayWithSupportedLocaleNoCountryCode?.theme)

        val displayNoSupportedLocaleAndNoFallback = getLocalizedAndThemedDisplay(
            credentialDisplays = noSupportedLocaleAndNoFallback,
            preferredTheme = Theme.LIGHT
        )
        assertEquals("xx", displayNoSupportedLocaleAndNoFallback?.locale)
        assertEquals("dark", displayNoSupportedLocaleAndNoFallback?.theme)

        val displayWithFallbackAndNoSupportedLocale = getLocalizedAndThemedDisplay(
            credentialDisplays = withFallbackAndNoSupportedLocale,
            preferredTheme = Theme.LIGHT
        )
        assertEquals("fallback", displayWithFallbackAndNoSupportedLocale?.locale)
        assertEquals("dark", displayWithFallbackAndNoSupportedLocale?.theme)
    }

    @Test
    fun `App locale without country code returns CredentialDisplay in requested theme with best matching locale`() = runTest {
        coEvery { mockGetCurrentAppLocale() } returns LocaleCompat.of("de")
        val displayWithSupportedLocaleWithCountryCode = getLocalizedAndThemedDisplay(
            credentialDisplays = withSupportedLocaleWithCountryCode,
            preferredTheme = Theme.DARK
        )
        assertEquals("de-CH", displayWithSupportedLocaleWithCountryCode?.locale)
        assertEquals("dark", displayWithSupportedLocaleWithCountryCode?.theme)

        val displayWithSupportedLocaleWithDifferentCountryCode = getLocalizedAndThemedDisplay(
            credentialDisplays = withSupportedLocaleWithDifferentCountryCode,
            preferredTheme = Theme.DARK
        )
        assertEquals("de-DE", displayWithSupportedLocaleWithDifferentCountryCode?.locale)
        assertEquals("dark", displayWithSupportedLocaleWithDifferentCountryCode?.theme)

        val displayWithSupportedLocaleNoCountryCode = getLocalizedAndThemedDisplay(
            credentialDisplays = withSupportedLocaleNoCountryCode,
            preferredTheme = Theme.DARK
        )
        assertEquals("en", displayWithSupportedLocaleNoCountryCode?.locale)
        assertEquals("dark", displayWithSupportedLocaleNoCountryCode?.theme)

        val displayNoSupportedLocaleAndNoFallback = getLocalizedAndThemedDisplay(
            credentialDisplays = noSupportedLocaleAndNoFallback,
            preferredTheme = Theme.DARK
        )
        assertEquals("xx", displayNoSupportedLocaleAndNoFallback?.locale)
        assertEquals("dark", displayNoSupportedLocaleAndNoFallback?.theme)

        val displayWithFallbackAndNoSupportedLocale = getLocalizedAndThemedDisplay(
            credentialDisplays = withFallbackAndNoSupportedLocale,
            preferredTheme = Theme.DARK
        )
        assertEquals("fallback", displayWithFallbackAndNoSupportedLocale?.locale)
        assertEquals("dark", displayWithFallbackAndNoSupportedLocale?.theme)
    }

    @Test
    fun `App locale without country code returns CredentialDisplay in not requested theme with best matching locale`() = runTest {
        coEvery { mockGetCurrentAppLocale() } returns LocaleCompat.of("de")
        val displayWithSupportedLocaleWithCountryCode = getLocalizedAndThemedDisplay(
            credentialDisplays = withSupportedLocaleWithCountryCode,
            preferredTheme = Theme.LIGHT
        )
        assertEquals("de-CH", displayWithSupportedLocaleWithCountryCode?.locale)
        assertEquals("dark", displayWithSupportedLocaleWithCountryCode?.theme)

        val displayWithSupportedLocaleWithDifferentCountryCode = getLocalizedAndThemedDisplay(
            credentialDisplays = withSupportedLocaleWithDifferentCountryCode,
            preferredTheme = Theme.LIGHT
        )
        assertEquals("de-DE", displayWithSupportedLocaleWithDifferentCountryCode?.locale)
        assertEquals("dark", displayWithSupportedLocaleWithDifferentCountryCode?.theme)

        val displayWithSupportedLocaleNoCountryCode = getLocalizedAndThemedDisplay(
            credentialDisplays = withSupportedLocaleNoCountryCode,
            preferredTheme = Theme.LIGHT
        )
        assertEquals("en", displayWithSupportedLocaleNoCountryCode?.locale)
        assertEquals("dark", displayWithSupportedLocaleNoCountryCode?.theme)

        val displayNoSupportedLocaleAndNoFallback = getLocalizedAndThemedDisplay(
            credentialDisplays = noSupportedLocaleAndNoFallback,
            preferredTheme = Theme.LIGHT
        )
        assertEquals("xx", displayNoSupportedLocaleAndNoFallback?.locale)
        assertEquals("dark", displayNoSupportedLocaleAndNoFallback?.theme)

        val displayWithFallbackAndNoSupportedLocale = getLocalizedAndThemedDisplay(
            credentialDisplays = withFallbackAndNoSupportedLocale,
            preferredTheme = Theme.LIGHT
        )
        assertEquals("fallback", displayWithFallbackAndNoSupportedLocale?.locale)
        assertEquals("dark", displayWithFallbackAndNoSupportedLocale?.theme)
    }

    @Test
    fun `Empty input list returns empty list`() = runTest {
        coEvery { mockGetCurrentAppLocale() } returns LocaleCompat.of("de", "CH")

        assertNull(getLocalizedAndThemedDisplay(credentialDisplays = emptyList(), preferredTheme = Theme.LIGHT))
    }
}
