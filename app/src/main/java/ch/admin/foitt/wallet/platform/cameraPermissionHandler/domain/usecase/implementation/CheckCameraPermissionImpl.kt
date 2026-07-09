package ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.usecase.implementation

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.model.PermissionState
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.usecase.CheckCameraPermission
import timber.log.Timber
import javax.inject.Inject

class CheckCameraPermissionImpl @Inject constructor() : CheckCameraPermission {
    @CheckResult
    override suspend fun invoke(
        permissionsAreGranted: Boolean,
        rationaleShouldBeShown: Boolean,
        introPromptWasAccepted: Boolean,
        autoPromptWasTriggered: Boolean,
        manualPromptWasTriggered: Boolean,
        rationaleWasShown: Boolean,
    ): PermissionState {
        Timber.d(
            """
                Permission result:
                :: granted: $permissionsAreGranted
                :: rationale should be shown: $rationaleShouldBeShown
                :: introPromptWasAccepted: $introPromptWasAccepted
                :: auto prompt was triggered: $autoPromptWasTriggered
                :: manual prompt was triggered: $manualPromptWasTriggered
                :: rationale was shown: $rationaleWasShown
            """.trimIndent()
        )

        return when {
            permissionsAreGranted -> PermissionState.Granted
            rationaleShouldBeShown -> PermissionState.Rationale
            rationaleWasShown -> PermissionState.Blocked
            !introPromptWasAccepted && !manualPromptWasTriggered -> PermissionState.Intro
            !autoPromptWasTriggered && !manualPromptWasTriggered -> PermissionState.AutoPrompt
            !manualPromptWasTriggered -> PermissionState.ManualPrompt
            // Permission were refused after an active prompt
            // but no rationale was asked
            // Can happen for various reasons, like
            // - permissions were toggled/changed outside the app
            // - the system reset the permissions after some time
            // - the prompt was denied after a rationale
            // The state is not always consistent, but we have to assume the permission could be permanently denied
            // As of vanilla android 11.
            else -> PermissionState.Blocked
        }
    }
}
