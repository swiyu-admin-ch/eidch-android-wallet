package ch.admin.foitt.wallet.feature.qrscan.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import ch.admin.foitt.wallet.platform.genericScreens.BaseScreen
import ch.admin.foitt.wallet.platform.utils.TestTags

class QrScannerScreen(composeTestRule: ComposeContentTestRule) : BaseScreen(composeTestRule) {
    private val scanningView = composeTestRule.onNodeWithTag(TestTags.SCANNING_VIEW.name)

    @OptIn(ExperimentalTestApi::class)
    override fun isDisplayed() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(TestTags.SCANNING_VIEW.name), 20000)
        scanningView.assertIsDisplayed()
    }
}
