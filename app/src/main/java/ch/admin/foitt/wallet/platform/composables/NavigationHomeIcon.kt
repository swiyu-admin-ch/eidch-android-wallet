package ch.admin.foitt.wallet.platform.composables

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.utils.TestTags

@Composable
fun NavigationHomeIcon(
    isBackbuttonVisible: Boolean = false,
    onBackbuttonClick: () -> Unit = {},
) {
    if (isBackbuttonVisible) {
        IconButton(
            modifier = Modifier.testTag(TestTags.BACK_BUTTON.name),
            onClick = onBackbuttonClick
        ) {
            Icon(
                painterResource(R.drawable.wallet_ic_back_navigation),
                contentDescription = "Back"
            )
        }
    } else {
        IconButton(onClick = { }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_swiss_cross_small),
                tint = Color.Unspecified,
                contentDescription = "Home"
            )
        }
    }
}
