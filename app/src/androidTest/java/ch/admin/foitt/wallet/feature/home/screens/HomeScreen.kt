package ch.admin.foitt.wallet.feature.home.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.admin.foitt.wallet.platform.genericScreens.BaseScreen
import ch.admin.foitt.wallet.platform.utils.TestTags

class HomeScreen(composeTestRule: ComposeContentTestRule) : BaseScreen(composeTestRule) {
    private val noCredentialIcon = composeTestRule.onNodeWithTag(TestTags.NO_CREDENTIAL_ICON.name)
    private val menuButton = composeTestRule.onNodeWithTag(TestTags.MENU_BUTTON.name)
    private val scanButton = composeTestRule.onNodeWithTag(TestTags.SCAN_TEXT_BUTTON.name)
    private val credentialList = composeTestRule.onNodeWithTag(TestTags.CREDENTIAL_LIST.name)

    @OptIn(ExperimentalTestApi::class)
    override fun isDisplayed() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(TestTags.MENU_BUTTON.name), 10000)
        menuButton.assertIsDisplayed()
        scanButton.assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    fun isDisplayedEmpty() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(TestTags.NO_CREDENTIAL_ICON.name), 10000)
        noCredentialIcon.assertIsDisplayed()
        credentialList.assertIsNotDisplayed()
        menuButton.assertIsDisplayed()
        scanButton.assertIsDisplayed()
    }

    fun isDisplayedWithCredentials() {
        noCredentialIcon.assertIsNotDisplayed()
        credentialList.assertIsDisplayed()
        menuButton.assertIsDisplayed()
        scanButton.assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    fun credentialListIsDisplayed() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(TestTags.CREDENTIAL_LIST.name), 10000)
        noCredentialIcon.assertIsNotDisplayed()
        credentialList.assertIsDisplayed()
        menuButton.assertIsDisplayed()
        scanButton.assertIsDisplayed()
    }

    fun openScanner() {
        scanButton.apply {
            assertIsDisplayed()
            performClick()
        }
    }
}
