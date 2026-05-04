package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import ch.admin.foitt.wallet.platform.utils.lockOrientation
import ch.admin.foitt.wallet.platform.utils.unlockOrientation

@Composable
fun OrientationLocker(currentActivity: AppCompatActivity, shouldLock: Boolean) {
    DisposableEffect(shouldLock) {
        if (shouldLock) {
            currentActivity.lockOrientation()
        } else {
            currentActivity.unlockOrientation()
        }

        onDispose {
            currentActivity.unlockOrientation()
        }
    }
}
