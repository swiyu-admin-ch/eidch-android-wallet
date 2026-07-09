package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.GetEIdRequestCase
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdUiDocumentType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toEIdDocumentType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.GetDocumentType
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.DestinationGroup
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import com.github.michaelbull.result.mapOrElse
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow

@HiltViewModel(assistedFactory = EIdDocumentScannerInfoViewModel.Factory::class)
internal class EIdDocumentScannerInfoViewModel @AssistedInject constructor(
    private val navManager: NavigationManager,
    @Assisted private val caseId: String,
    getDocumentType: GetDocumentType,
    getEIdRequestCase: GetEIdRequestCase,
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

    val documentType: StateFlow<EIdUiDocumentType> = if (isFirstDocScan) {
        getDocumentType()
    } else {
        flow {
            val documentType = getEIdRequestCase(caseId).mapOrElse(
                default = { EIdUiDocumentType.IDENTITY_CARD },
                transform = { it.selectedDocumentType.toEIdDocumentType() }
            )
            emit(documentType)
        }.toStateFlow(EIdUiDocumentType.IDENTITY_CARD)
    }

    private val isFirstDocScan: Boolean get() = caseId.isBlank()

    fun onContinue() = navManager.navigateTo(Destination.EIdDocumentScannerScreen(caseId = caseId))

    private fun onBack() = navManager.popBackStack()

    private fun onClose() = when {
        caseId.isEmpty() -> navManager.navigateOutOf(DestinationGroup.EIdApplicationProcess::class)
        else -> navManager.navigateOutOf(DestinationGroup.EIdRequestVerification::class)
    }
}
