package ch.admin.foitt.wallet.feature.credentialDetail.presentation

import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.GetCredentialIssuerDisplaysFlow
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.model.UpdateCredentialUiState
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetDrawableFromUri
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.toPainter
import com.github.michaelbull.result.get
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

@HiltViewModel(assistedFactory = UpdateCredentialViewModel.Factory::class)
class UpdateCredentialViewModel @AssistedInject constructor(
    private val navManager: NavigationManager,
    private val getDrawableFromUri: GetDrawableFromUri,
    getCredentialIssuerDisplaysFlow: GetCredentialIssuerDisplaysFlow,
    setTopBarState: SetTopBarState,
    @Assisted val credentialId: Long,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.Details(onUp = navManager::popBackStack, titleId = null)

    @AssistedFactory
    interface Factory {
        fun create(credentialId: Long): UpdateCredentialViewModel
    }

    private val credentialIssuerUiState = getCredentialIssuerDisplaysFlow(credentialId)

    val uiState: StateFlow<UpdateCredentialUiState> = credentialIssuerUiState.map { issuerDisplayResult ->
        val issuerDisplay = issuerDisplayResult.get()
        when {
            issuerDisplay != null -> UpdateCredentialUiState(
                issuerName = issuerDisplay.name,
                issuerPainter = issuerDisplay.image.toPainter(),
            )
            else -> UpdateCredentialUiState()
        }
    }.toStateFlow(UpdateCredentialUiState())

    fun onUpdate() {
    }

    private suspend fun String?.toPainter() = let { getDrawableFromUri(it)?.toPainter() }
}
