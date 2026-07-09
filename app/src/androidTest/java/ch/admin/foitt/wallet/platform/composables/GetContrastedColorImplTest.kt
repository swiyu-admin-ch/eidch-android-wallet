package ch.admin.foitt.wallet.platform.composables

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.ColorUtils
import androidx.test.core.app.ApplicationProvider
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.implementation.GetColorImpl
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.implementation.GetContrastedColorImpl
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions

class GetContrastedColorImplTest {
    private lateinit var getContrastedColors: GetContrastedColorImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        val getColor = GetColorImpl()
        getColor(Color.Black.toString())

        getContrastedColors = GetContrastedColorImpl(
            appContext = ApplicationProvider.getApplicationContext(),
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun similarContrastResultInDarkContent() = runTest {
        mockkStatic(ColorUtils::class)
        coEvery { ColorUtils.calculateContrast(any(), any()) } returns 3.0
        val darkContentColor = Color.DarkGray

        val result = getContrastedColors(
            backgroundColor = Color.Gray,
            backgroundOverlayColor = Color.Unspecified,
            darkContentColor = darkContentColor,
        )

        Assertions.assertEquals(darkContentColor, result, "light text used, expecting dark")
    }

    @Test
    fun betterDarkContrastResultInDarkContent() = runTest {
        val darkContentColor = Color.DarkGray
        val result = getContrastedColors(
            backgroundColor = Color.White,
            backgroundOverlayColor = Color.Unspecified,
            darkContentColor = darkContentColor,

        )

        Assertions.assertEquals(darkContentColor, result, "light text used, expecting dark")
    }

    @Test
    fun betterLightContrastResultInLightContent() = runTest {
        val lightContentColor = Color.LightGray
        val result = getContrastedColors(
            backgroundColor = Color.Black,
            backgroundOverlayColor = Color.Unspecified,
            lightContentColor = lightContentColor,
        )

        Assertions.assertEquals(lightContentColor, result, "dark text used, expecting light")
    }

    @Test
    fun elfaBlueShouldHaveLightContent() = runTest {
        val elfaBlue = Color(0xFF007AFF)
        val lightContentColor = Color.LightGray
        val result = getContrastedColors(
            backgroundColor = elfaBlue,
            lightContentColor = lightContentColor,
        )

        Assertions.assertEquals(lightContentColor, result, "dark text used, expecting light")
    }
}
