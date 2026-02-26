package ch.admin.foitt.wallet.platform.versionEnforcement.presentation

import android.content.Context
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openLink
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel(assistedFactory = AppVersionBlockedViewModel.Factory::class)
class AppVersionBlockedViewModel @AssistedInject constructor(
    @param:ApplicationContext private val appContext: Context,
    setTopBarState: SetTopBarState,
    @Assisted("title") val title: String?,
    @Assisted("text") val text: String?
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.None

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("title") title: String?,
            @Assisted("text") text: String?
        ): AppVersionBlockedViewModel
    }

    fun goToPlayStore() = appContext.openLink(R.string.version_enforcement_store_link)
}
