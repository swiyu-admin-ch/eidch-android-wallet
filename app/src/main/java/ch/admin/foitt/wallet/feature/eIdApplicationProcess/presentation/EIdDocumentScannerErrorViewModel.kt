package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.DocumentScannerErrorType
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel(assistedFactory = EIdDocumentScannerErrorViewModel.Factory::class)
internal class EIdDocumentScannerErrorViewModel @AssistedInject constructor(
    private val navManager: NavigationManager,
    @Assisted private val type: DocumentScannerErrorType,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.Empty

    @AssistedFactory
    interface Factory {
        fun create(type: DocumentScannerErrorType): EIdDocumentScannerErrorViewModel
    }

    fun onClose() = navManager.popBackStackOrToRoot()

    val errorType = type
}
