package ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase



import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.swipeUp
import ch.admin.foitt.wallet.platform.genericScreens.BaseScreen
import ch.admin.foitt.wallet.platform.utils.TestTags

class CredentialOfferScreen(composeTestRule: ComposeContentTestRule) : BaseScreen(composeTestRule) {
    private val credential = composeTestRule.onNodeWithTag(TestTags.OFFER_CREDENTIAL.name, useUnmergedTree = true)
    private val acceptButton = composeTestRule.onNodeWithTag(TestTags.ACCEPT_BUTTON.name)
    private val declineButton = composeTestRule.onNodeWithTag(TestTags.DECLINE_BUTTON.name)
    private val stickyAcceptButton = composeTestRule.onNodeWithTag(TestTags.STICKY_ACCEPT_BUTTON.name)
    private val stickyDeclineButton = composeTestRule.onNodeWithTag(TestTags.STICKY_DECLINE_BUTTON.name)
    private val issuerIcon = composeTestRule.onNodeWithTag(TestTags.ISSUER_ICON.name)
    private val issuerName = composeTestRule.onNodeWithTag(TestTags.ISSUER_NAME.name)
    private val verifiedBadge = composeTestRule.onNodeWithTag(TestTags.VERIFIED_BADGE.name)
    private val unverifiedBadge = composeTestRule.onNodeWithTag(TestTags.UNVERIFIED_BADGE.name)
    private val verifiedText = composeTestRule.onNodeWithTag(TestTags.VERIFIED_TEXT.name)
    private val unverifiedText = composeTestRule.onNodeWithTag(TestTags.UNVERIFIED_TEXT.name)
    private val wrongDataLink = composeTestRule.onNodeWithTag(TestTags.WRONG_DATA_LINK.name, useUnmergedTree = true)
    private val photo = composeTestRule.onNodeWithText("Photo", useUnmergedTree = true)

    @OptIn(ExperimentalTestApi::class)
    override fun isDisplayed() {
        composeTestRule.onRoot(useUnmergedTree = true).printToLog("home")
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(TestTags.OFFER_CREDENTIAL.name), 10000)
        credential.assertIsDisplayed()
        acceptButton.assertIsDisplayed()
        declineButton.assertIsDisplayed()
    }

    fun isVerified() {
        verifiedBadge.isDisplayed()
        verifiedText.isDisplayed()
    }

    fun isNotVerified() {
        unverifiedBadge.isDisplayed()
        unverifiedText.isDisplayed()
    }

    fun issuerDisplayed() {
        issuerIcon.isDisplayed()
        issuerName.isDisplayed()
    }

    fun acceptCredential() {
        acceptButton.apply {
            assertIsDisplayed()
            performClick()
        }
    }

    fun declineCredential() {
        declineButton.apply {
            assertIsDisplayed()
            performClick()
        }
    }
    @OptIn(ExperimentalTestApi::class)
    fun acceptStickyCredential() {
        scrollDown()
        scrollDown()
        acceptButton.apply {
            assertIsDisplayed()
            performClick()
        }
    }

    fun declineStickyCredential() {
        scrollDown()
        scrollDown()
        declineButton.apply {
            assertIsDisplayed()
            performClick()
        }
    }

    fun reportWrongData() {
        scrollDown()
        wrongDataLink.apply {
            assertIsDisplayed()
            performClick()
        }
    }

    @OptIn(ExperimentalTestApi::class)
    fun hasDetails() {
        photo.isDisplayed()
        composeTestRule.onRoot().performTouchInput {
            swipeUp(bottom, top, 2000)
        }
        composeTestRule.onRoot(useUnmergedTree = true).printToLog("after Scroll")
        composeTestRule.waitUntilAtLeastOneExists(hasText("First name"), 10000)
        composeTestRule.waitUntilAtLeastOneExists(hasText("Last name"), 10000)
    }
}
