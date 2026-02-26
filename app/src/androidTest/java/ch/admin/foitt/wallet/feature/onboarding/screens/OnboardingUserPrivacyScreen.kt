package ch.admin.foitt.wallet.feature.onboarding.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import ch.admin.foitt.wallet.platform.genericScreens.BaseScreen
import ch.admin.foitt.wallet.platform.utils.TestTags

class OnboardingUserPrivacyScreen(composeTestRule: ComposeContentTestRule) : BaseScreen(composeTestRule) {

    val image = composeTestRule.onNodeWithTag(TestTags.USER_PRIVACY_ICON.name)
    val acceptButton = composeTestRule.onNodeWithTag(TestTags.ACCEPT_BUTTON.name)
    val declineButton = composeTestRule.onNodeWithTag(TestTags.DECLINE_BUTTON.name)
    val backButton = composeTestRule.onNodeWithTag(TestTags.BACK_BUTTON.name)

    fun clickBack() {
        backButton.apply {
            assertIsDisplayed()
            performClick()
        }
    }

    @OptIn(ExperimentalTestApi::class)
    override fun isDisplayed() {
        composeTestRule.onRoot().printToLog("PRINT")
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(TestTags.USER_PRIVACY_ICON.name), 10000)
        image.assertIsDisplayed()
        acceptButton.assertIsDisplayed()
        declineButton.assertIsDisplayed()
    }

    fun accept() {
        acceptButton.apply {
            assertIsDisplayed()
            performClick()
        }
    }

    fun decline() {
        declineButton.apply {
            assertIsDisplayed()
            performClick()
        }
    }

    fun navigateToScreen() {
        val presentScreen = OnboardingPresentScreen(composeTestRule)
        presentScreen.navigateToScreen()
        presentScreen.nextScreen()
        isDisplayed()
    }

}
