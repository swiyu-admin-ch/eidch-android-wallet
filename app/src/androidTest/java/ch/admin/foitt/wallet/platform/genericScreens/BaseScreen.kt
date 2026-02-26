package ch.admin.foitt.wallet.platform.genericScreens

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp

open class BaseScreen(val composeTestRule: ComposeContentTestRule) {

    open fun isDisplayed() {}

    fun swipeBack() {
        composeTestRule.onRoot().performTouchInput {
            swipeRight()
        }
    }

    fun scrollDown() {
        composeTestRule.onRoot().performTouchInput {
            swipeUp()
        }
    }

    fun scrollUp() {
        composeTestRule.onRoot().performTouchInput {
            swipeDown()
        }
    }
}
