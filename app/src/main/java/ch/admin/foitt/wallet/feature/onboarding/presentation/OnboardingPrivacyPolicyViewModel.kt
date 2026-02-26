package ch.admin.foitt.wallet.feature.onboarding.presentation

import android.content.Context
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.eventTracking.domain.usecase.ApplyUserPrivacyPolicy
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openLink
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class OnboardingPrivacyPolicyViewModel @Inject constructor(
    private val navManager: NavigationManager,
    @param:ApplicationContext private val appContext: Context,
    setTopBarState: SetTopBarState,
    private val applyUserPrivacyPolicy: ApplyUserPrivacyPolicy,
) : ScreenViewModel(setTopBarState) {

    override val topBarState = TopBarState.Details(onUp = navManager::popBackStack, null)

    fun acceptTracking() {
        applyUserPrivacyPolicy(true)
        onNext()
    }

    fun declineTracking() {
        applyUserPrivacyPolicy(false)
        onNext()
    }

    fun onOpenUserPrivacyPolicy() = appContext.openLink(R.string.tk_onboarding_analytics_tertiary_link_value)

    private fun onNext() = navManager.navigateTo(Destination.OnboardingPassphraseExplanationScreen)
    fun onBack() = navManager.popBackStack()
}
