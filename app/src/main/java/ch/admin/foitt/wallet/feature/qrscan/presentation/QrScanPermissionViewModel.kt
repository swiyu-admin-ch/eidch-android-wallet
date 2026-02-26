package ch.admin.foitt.wallet.feature.qrscan.presentation

import android.Manifest
import android.content.Context
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.platform.appSetupState.domain.usecase.GetFirstCredentialWasAdded
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.model.PermissionState
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.usecase.CheckCameraPermission
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.usecase.ShouldAutoTriggerPermissionPrompt
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.extension.hasCameraPermission
import ch.admin.foitt.wallet.platform.scaffold.extension.shouldShowRationale
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openAppDetailsSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject

@HiltViewModel
class QrScanPermissionViewModel @Inject constructor(
    private val checkCameraPermission: CheckCameraPermission,
    private val shouldAutoTriggerPermissionPrompt: ShouldAutoTriggerPermissionPrompt,
    private val getFirstCredentialWasAdded: GetFirstCredentialWasAdded,
    private val navManager: NavigationManager,
    @param:ApplicationContext private val appContext: Context,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    override val topBarState = TopBarState.Details(navManager::popBackStack, null)

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
        navManager.popBackStack()
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

    fun onClose() = navManager.popBackStack()

    private suspend fun handleNewState(newState: PermissionState) = when (newState) {
        PermissionState.Granted -> navManager.replaceCurrentWith(
            Destination.QrScannerScreen(
                firstCredentialWasAdded = getFirstCredentialWasAdded()
            )
        )
        PermissionState.Blocked,
        PermissionState.Initial,
        PermissionState.Intro,
        PermissionState.Rationale -> _permissionState.update { newState }
    }
}
