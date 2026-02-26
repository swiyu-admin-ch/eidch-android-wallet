package ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.admin.foitt.wallet.platform.genericScreens.BaseScreen
import ch.admin.foitt.wallet.platform.utils.TestTags

class DeclineOfferScreen(composeTestRule: ComposeContentTestRule) : BaseScreen(composeTestRule) {
    private val title = composeTestRule.onNodeWithTag(TestTags.DECLINE_SCREEN_TITLE.name)
    private val acceptButton = composeTestRule.onNodeWithTag(TestTags.ACCEPT_BUTTON.name)
    private val declineButton = composeTestRule.onNodeWithTag(TestTags.DECLINE_BUTTON.name)


    @OptIn(ExperimentalTestApi::class)
    override fun isDisplayed() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(TestTags.ACCEPT_BUTTON.name), 10000)
        acceptButton.assertIsDisplayed()
        declineButton.assertIsDisplayed()
    }

    fun confirmDecline() {
        acceptButton.apply {
            assertIsDisplayed()
            performClick()
        }
    }

    fun cancelDecline() {
        declineButton.apply {
            assertIsDisplayed()
            performClick()
        }
    }
}
