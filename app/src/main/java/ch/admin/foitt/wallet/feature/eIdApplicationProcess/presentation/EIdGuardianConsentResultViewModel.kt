package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.GuardianConsentResultState
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.asDayFullMonthYear
import ch.admin.foitt.wallet.platform.utils.asDayFullMonthYearHoursMinutes
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@HiltViewModel(assistedFactory = EIdGuardianConsentResultViewModel.Factory::class)
class EIdGuardianConsentResultViewModel @AssistedInject constructor(
    private val navManager: NavigationManager,
    private val getCurrentAppLocale: GetCurrentAppLocale,
    @Assisted private val rawDeadline: String?,
    @Assisted val screenState: GuardianConsentResultState,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.EmptyWithCloseButton(
        onClose = { navManager.navigateBackToHomeScreen(Destination.EIdIntroScreen::class) }
    )

    @AssistedFactory
    interface Factory {
        fun create(screenState: GuardianConsentResultState, rawDeadline: String?): EIdGuardianConsentResultViewModel
    }

    private val date = rawDeadline?.let {
        ZonedDateTime.parse(it, DateTimeFormatter.ISO_ZONED_DATE_TIME)
    }

    val formattedDate: String?
        get() = date?.let {
            when (screenState) {
                GuardianConsentResultState.QUEUEING_LEGAL_CONSENT_OK -> date.asDayFullMonthYear(getCurrentAppLocale())
                GuardianConsentResultState.AV_READY_LEGAL_CONSENT_PENDING -> date.asDayFullMonthYearHoursMinutes(getCurrentAppLocale())
                GuardianConsentResultState.QUEUEING_LEGAL_CONSENT_PENDING,
                GuardianConsentResultState.AV_EXPIRED_LEGAL_CONSENT_PENDING -> null
            }
        }

    fun onNext() = navManager.navigateBackToHomeScreen(Destination.EIdIntroScreen::class)
}
