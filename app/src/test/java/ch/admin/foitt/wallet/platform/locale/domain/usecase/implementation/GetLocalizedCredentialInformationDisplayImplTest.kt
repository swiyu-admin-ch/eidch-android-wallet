package ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.OidIssuerDisplay
import ch.admin.foitt.wallet.platform.locale.LocaleCompat
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedCredentialInformationDisplay
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

class GetLocalizedCredentialInformationDisplayImplTest {

    @MockK
    private lateinit var mockGetCurrentAppLocale: GetCurrentAppLocale
    private lateinit var useCase: GetLocalizedCredentialInformationDisplay

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = GetLocalizedCredentialInformationDisplayImpl(getCurrentAppLocale = mockGetCurrentAppLocale)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `should return LocalizedDisplay with best matching locale for app locale with supported country code`() = runTest {
        coEvery { mockGetCurrentAppLocale() } returns LocaleCompat.of("de", "CH")

        assertEquals("de-CH", useCase(withSupportedLocaleWithCountryCode)?.locale)
        assertEquals("de-DE", useCase(withSupportedLocaleWithDifferentCountryCode)?.locale)
        assertEquals("en", useCase(withSupportedLocaleNoCountryCode)?.locale)
        assertEquals("xx", useCase(noSupportedLocaleAndNoFallback)?.locale)
        assertEquals("fallback", useCase(withFallbackAndNoSupportedLocale)?.locale)
    }

    @Test
    fun `should return LocalizedDisplay with best matching locale for app locale with unsupported country code`() = runTest {
        coEvery { mockGetCurrentAppLocale() } returns LocaleCompat.of("de", "XX")

        assertEquals("de-CH", useCase(withSupportedLocaleWithCountryCode)?.locale)
        assertEquals("de-DE", useCase(withSupportedLocaleWithDifferentCountryCode)?.locale)
        assertEquals("en", useCase(withSupportedLocaleNoCountryCode)?.locale)
        assertEquals("xx", useCase(noSupportedLocaleAndNoFallback)?.locale)
        assertEquals("fallback", useCase(withFallbackAndNoSupportedLocale)?.locale)
    }

    @Test
    fun `should return LocalizedDisplay with best matching locale for app locale without country code`() = runTest {
        coEvery { mockGetCurrentAppLocale() } returns LocaleCompat.of("de")

        assertEquals("de-CH", useCase(withSupportedLocaleWithCountryCode)?.locale)
        assertEquals("de-DE", useCase(withSupportedLocaleWithDifferentCountryCode)?.locale)
        assertEquals("en", useCase(withSupportedLocaleNoCountryCode)?.locale)
        assertEquals("xx", useCase(noSupportedLocaleAndNoFallback)?.locale)
        assertEquals("fallback", useCase(withFallbackAndNoSupportedLocale)?.locale)
    }

    @Test
    fun `should return null for empty input collection`() = runTest {
        coEvery { mockGetCurrentAppLocale() } returns LocaleCompat.of("de", "CH")

        assertNull(useCase(emptyList()))
    }

    val withSupportedLocaleWithCountryCode = listOf("en", "fr", "de-CH", "fallback").map { OidIssuerDisplay(locale = it) }
    val withSupportedLocaleWithDifferentCountryCode = listOf("en", "fr", "de-DE", "fallback").map {
        OidIssuerDisplay(locale = it)
    }
    val withSupportedLocaleNoCountryCode = listOf("en", "it", "fallback", "yy").map { OidIssuerDisplay(locale = it) }
    val noSupportedLocaleAndNoFallback = listOf("xx", "yy").map { OidIssuerDisplay(locale = it) }
    val withFallbackAndNoSupportedLocale = listOf("yy", "fallback").map { OidIssuerDisplay(locale = it) }
}
