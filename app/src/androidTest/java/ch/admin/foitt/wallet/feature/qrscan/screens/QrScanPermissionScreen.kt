package ch.admin.foitt.wallet.feature.qrscan.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.admin.foitt.wallet.platform.genericScreens.BaseScreen
import ch.admin.foitt.wallet.platform.utils.TestTags

class QrScanPermissionScreen(composeTestRule: ComposeContentTestRule) : BaseScreen(composeTestRule) {
    private val permissionIntroIcon = composeTestRule.onNodeWithTag(TestTags.PERMISSION_INTRO_ICON.name)
    private val acceptButton = composeTestRule.onNodeWithTag(TestTags.ACCEPT_BUTTON.name)

    @OptIn(ExperimentalTestApi::class)
    override fun isDisplayed() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(TestTags.PERMISSION_INTRO_ICON.name), 10000)
        permissionIntroIcon.assertIsDisplayed()
        acceptButton.assertIsDisplayed()
    }

    fun nextScreen() {
        acceptButton.apply {
            assertIsDisplayed()
            performClick()
        }
    }
}
