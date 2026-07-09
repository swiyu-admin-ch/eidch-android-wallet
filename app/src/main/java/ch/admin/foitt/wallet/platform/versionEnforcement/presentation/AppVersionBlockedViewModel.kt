package ch.admin.foitt.wallet.platform.versionEnforcement.presentation

import android.content.Context
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.deeplink.domain.usecase.HandleDeeplink
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openLink
import ch.admin.foitt.wallet.platform.utils.openPhoneSettings
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.EnforcementType
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

@HiltViewModel(assistedFactory = AppVersionBlockedViewModel.Factory::class)
class AppVersionBlockedViewModel @AssistedInject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val handleDeeplink: HandleDeeplink,
    setTopBarState: SetTopBarState,
    @Assisted("title") val initialTitle: String?,
    @Assisted("text") val initialText: String?,
    @Assisted("playStoreUrl") val playStoreUrl: String?,
    @Assisted("enforcedType") val enforcedType: EnforcementType
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.None

    val title: String? = when (enforcedType) {
        EnforcementType.OS_UPDATE -> appContext.getString(R.string.tk_versionEnforcement_systemUpdate_title)
        EnforcementType.DEVICE_BLACKLIST -> appContext.getString(R.string.tk_versionEnforcement_blacklisted_title)
        else -> initialTitle
    }

    val text: String? = when (enforcedType) {
        EnforcementType.OS_UPDATE -> appContext.getString(R.string.tk_versionEnforcement_systemUpdate_content)
        EnforcementType.DEVICE_BLACKLIST -> appContext.getString(R.string.tk_versionEnforcement_blacklisted_content)
        else -> initialText
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("title") title: String?,
            @Assisted("text") text: String?,
            @Assisted("playStoreUrl") playStoreUrl: String?,
            @Assisted("enforcedType") enforcedType: EnforcementType
        ): AppVersionBlockedViewModel
    }

    fun goToPlayStore() {
        if (playStoreUrl != null) {
            appContext.openLink(playStoreUrl)
        } else {
            appContext.openLink(R.string.version_enforcement_store_link)
        }
    }

    fun onClose() {
        exitProcess(0)
    }

    fun onSettings() {
        appContext.openPhoneSettings()
    }

    fun onContinue() {
        viewModelScope.launch {
            handleDeeplink(fromOnboarding = false).navigate()
        }
    }
}
