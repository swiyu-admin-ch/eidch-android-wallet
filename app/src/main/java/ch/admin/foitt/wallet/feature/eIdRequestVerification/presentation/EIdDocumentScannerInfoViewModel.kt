package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.DestinationGroup
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel(assistedFactory = EIdDocumentScannerInfoViewModel.Factory::class)
internal class EIdDocumentScannerInfoViewModel @AssistedInject constructor(
    private val navManager: NavigationManager,
    @Assisted private val caseId: String,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    override val topBarState = TopBarState.DetailsWithCloseButton(
        titleId = null,
        onUp = ::onBack,
        onClose = ::onClose,
    )

    @AssistedFactory
    interface Factory {
        fun create(caseId: String): EIdDocumentScannerInfoViewModel
    }

    fun onContinue() = navManager.navigateTo(Destination.MrzScanPermissionScreen(caseId = caseId))

    private fun onBack() = navManager.popBackStack()

    private fun onClose() = when {
        caseId.isEmpty() -> navManager.navigateOutOf(DestinationGroup.EIdApplicationProcess::class)
        else -> navManager.navigateOutOf(DestinationGroup.EIdRequestVerification::class)
    }
}
