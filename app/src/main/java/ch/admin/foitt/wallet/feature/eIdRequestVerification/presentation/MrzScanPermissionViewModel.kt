package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import android.Manifest
import android.content.Context
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.model.PermissionState
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.usecase.CheckCameraPermission
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.usecase.ShouldAutoTriggerPermissionPrompt
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.DestinationGroup
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.extension.hasCameraPermission
import ch.admin.foitt.wallet.platform.scaffold.extension.shouldShowRationale
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openAppDetailsSettings
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.ref.WeakReference

@HiltViewModel(assistedFactory = MrzScanPermissionViewModel.Factory::class)
class MrzScanPermissionViewModel @AssistedInject constructor(
    private val checkCameraPermission: CheckCameraPermission,
    private val shouldAutoTriggerPermissionPrompt: ShouldAutoTriggerPermissionPrompt,
    private val navManager: NavigationManager,
    @Assisted private val caseId: String,
    @param:ApplicationContext private val appContext: Context,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    override val topBarState = TopBarState.DetailsWithCloseButton(
        titleId = null,
        onUp = navManager::popBackStack,
        onClose = ::onClose
    )

    @AssistedFactory
    interface Factory {
        fun create(caseId: String): MrzScanPermissionViewModel
    }

    private val cameraPermission by lazy { Manifest.permission.CAMERA }

    private var autoPromptWasTriggered = false

    private val _permissionState = MutableStateFlow<PermissionState>(PermissionState.Initial)
    val permissionState = _permissionState.asStateFlow()

    private var currentPermissionLauncher: WeakReference<ManagedActivityResultLauncher<String, Boolean>> =
        WeakReference(null)

    fun setPermissionLauncher(permissionLauncher: ManagedActivityResultLauncher<String, Boolean>) {
        currentPermissionLauncher = WeakReference(permissionLauncher)
    }

    fun onCameraPermissionResult(permissionGranted: Boolean, activity: FragmentActivity) {
        val shouldShowRationale = shouldShowRationale(activity)
        viewModelScope.launch {
            checkCameraPermission(
                permissionsAreGranted = permissionGranted,
                rationaleShouldBeShown = shouldShowRationale,
                promptWasTriggered = true,
            ).let { handleNewState(it) }
        }
    }

    fun onCameraPermissionPrompt() {
        currentPermissionLauncher.get()?.launch(cameraPermission)
            ?: Timber.e("PermissionLauncher was null when attempting prompt")
    }

    fun onOpenSettings() {
        appContext.openAppDetailsSettings()
    }

    fun navigateToFirstScreen(activity: FragmentActivity) {
        val hasPermission = hasCameraPermission(activity.applicationContext)
        val shouldShowRationale = shouldShowRationale(activity)
        viewModelScope.launch {
            if (shouldAutoTriggerPermissionPrompt() && !autoPromptWasTriggered) {
                autoPromptWasTriggered = true
                onCameraPermissionPrompt()
            } else {
                checkCameraPermission(
                    permissionsAreGranted = hasPermission,
                    rationaleShouldBeShown = shouldShowRationale,
                    promptWasTriggered = false,
                ).let { handleNewState(it) }
            }
        }
    }

    private fun onClose() = when {
        caseId.isEmpty() -> navManager.navigateOutOf(DestinationGroup.EIdApplicationProcess::class)
        else -> navManager.navigateOutOf(DestinationGroup.EIdRequestVerification::class)
    }

    private fun handleNewState(newState: PermissionState) = when (newState) {
        PermissionState.Granted -> navManager.replaceCurrentWith(Destination.EIdDocumentScannerScreen(caseId = caseId))
        PermissionState.Blocked,
        PermissionState.Initial,
        PermissionState.Intro,
        PermissionState.Rationale -> _permissionState.update { newState }
    }
}
