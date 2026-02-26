package ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase

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


class CredentialOfferWrongDataScreen(composeTestRule: ComposeContentTestRule) : BaseScreen(composeTestRule) {
    private val title = composeTestRule.onNodeWithTag(TestTags.WRONG_DATA_TITLE.name)
    private val image = composeTestRule.onNodeWithTag(TestTags.WRONG_DATA_IMAGE.name)
    private val backButton = composeTestRule.onNodeWithTag(TestTags.BACK_BUTTON.name)


    @OptIn(ExperimentalTestApi::class)
    override fun isDisplayed() {
        composeTestRule.onRoot().printToLog("info")
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(TestTags.WRONG_DATA_IMAGE.name), 10000)
        image.assertIsDisplayed()
        title.assertIsDisplayed()
    }

    fun clickBack() {
        backButton.apply {
            assertIsDisplayed()
            performClick()
        }
    }
}
