package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import ch.admin.foitt.wallet.platform.composables.presentation.WindowHeightClass
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.windowHeightClass
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.utils.LocalActivity

@Composable
fun OrientationLocker() {
    val currentActivity = LocalActivity.current
    val isLockRequired = isLockRequired(currentActivity)

    DisposableEffect(Unit) {
        if (isLockRequired) {
            requestOrientationLockChange(currentActivity, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        }

        onDispose {
            if (currentActivity.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                return@onDispose
            }
            requestOrientationLockChange(currentActivity, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        }
    }
}

@Composable
private fun isLockRequired(activity: AppCompatActivity): Boolean {
    val hasCompactWidth = currentWindowAdaptiveInfo().windowWidthClass() == WindowWidthClass.COMPACT
    val isCurrentlyPortrait = activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    val hasCompactHeight = currentWindowAdaptiveInfo().windowHeightClass() == WindowHeightClass.COMPACT
    val isCurrentlyLandscape = activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    return (hasCompactWidth && isCurrentlyPortrait) || (hasCompactHeight && isCurrentlyLandscape)
}

private fun requestOrientationLockChange(activity: AppCompatActivity, screenRotation: Int) {
    activity.requestedOrientation = screenRotation
}
