package ch.admin.foitt.wallet.feature.onboarding.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import ch.admin.foitt.wallet.platform.genericScreens.BaseScreen
import ch.admin.foitt.wallet.platform.utils.TestTags

open class OnboardingBaseScreen(composeTestRule: ComposeContentTestRule): BaseScreen(composeTestRule) {
    open val imageIdentifier = TestTags.INTRO_ICON.name
    open val image = composeTestRule.onNodeWithTag(TestTags.INTRO_ICON.name)
    open val continueButton = composeTestRule.onNodeWithTag(TestTags.CONTINUE_BUTTON.name)

    @OptIn(ExperimentalTestApi::class)
    override fun isDisplayed() {
        composeTestRule.onRoot().printToLog("PRINT")
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(imageIdentifier),10000)
        image.isDisplayed()
        continueButton.isDisplayed()
    }

    fun nextScreen() {
        continueButton.apply {
            assertIsDisplayed()
            performClick()
        }
    }
}

