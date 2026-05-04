package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.asDayFullMonthYear
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@HiltViewModel(assistedFactory = EIdQueueViewModel.Factory::class)
class EIdQueueViewModel @AssistedInject constructor(
    private val navManager: NavigationManager,
    private val getCurrentAppLocale: GetCurrentAppLocale,
    setTopBarState: SetTopBarState,
    @Assisted private val rawDeadline: String?,
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(rawDeadline: String?): EIdQueueViewModel
    }

    override val topBarState = TopBarState.WithCloseButton(
        onClose = { navManager.navigateBackToHomeScreen(Destination.EIdIntroScreen::class) }
    )

    private val date = rawDeadline?.let {
        ZonedDateTime.parse(it, DateTimeFormatter.ISO_ZONED_DATE_TIME)
    }

    val formattedDate: String? get() = date?.asDayFullMonthYear(getCurrentAppLocale())

    fun onNext() = navManager.navigateBackToHomeScreen(Destination.EIdIntroScreen::class)
}
