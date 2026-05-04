package ch.admin.foitt.wallet.platform.genericScreens.presentation

import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.genericScreens.domain.model.GenericErrorScreenState
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel(assistedFactory = GenericErrorViewModel.Factory::class)
class GenericErrorViewModel @AssistedInject constructor(
    private val navManager: NavigationManager,
    @Assisted private val errorScreenState: GenericErrorScreenState,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.Empty

    @AssistedFactory
    interface Factory {
        fun create(error: GenericErrorScreenState): GenericErrorViewModel
    }

    val title = when (errorScreenState) {
        GenericErrorScreenState.GENERIC -> R.string.presentation_error_title
        else -> R.string.tk_credentialOffer_error_primary
    }

    val subtitle = when (errorScreenState) {
        GenericErrorScreenState.GENERIC -> R.string.presentation_error_message
        else -> R.string.tk_credentialOffer_error_secondary
    }

    val errorText = when (errorScreenState) {
        GenericErrorScreenState.GENERIC -> null
        else -> errorScreenState.name.lowercase()
    }

    val errorDescription = when (errorScreenState) {
        GenericErrorScreenState.GENERIC -> null
        GenericErrorScreenState.INVALID_REQUEST -> R.string.tk_credentialOffer_error_invalidRequest_description
        GenericErrorScreenState.INVALID_GRANT -> R.string.tk_credentialOffer_error_invalidGrant_description
        GenericErrorScreenState.INVALID_CLIENT -> R.string.tk_credentialOffer_error_invalidClient_description
        GenericErrorScreenState.INVALID_CREDENTIAL_REQUEST -> R.string.tk_credentialOffer_error_invalidCredentialRequest_description
        GenericErrorScreenState.UNKNOWN_CREDENTIAL_CONFIGURATION ->
            R.string.tk_credentialOffer_error_unknownCredentialConfiguration_description
        GenericErrorScreenState.UNKNOWN_CREDENTIAL_IDENTIFIER ->
            R.string.tk_credentialOffer_error_unknownCredentialIdentifier_description
        GenericErrorScreenState.INVALID_PROOF -> R.string.tk_credentialOffer_error_invalidProof_description
        GenericErrorScreenState.INVALID_NONCE -> R.string.tk_credentialOffer_error_invalidNonce_description
        GenericErrorScreenState.INVALID_ENCRYPTION_PARAMETERS ->
            R.string.tk_credentialOffer_error_invalidEncryptionParameters_description
        GenericErrorScreenState.CREDENTIAL_REQUEST_DENIED -> R.string.tk_credentialOffer_error_credentialRequestDenied_description
        GenericErrorScreenState.INVALID_TRANSACTION_ID -> R.string.tk_credentialOffer_error_invalidTransactionId_description
        GenericErrorScreenState.INVALID_REQUEST_BEARER_TOKEN -> R.string.tk_credentialOffer_error_invalidRequest_description
        GenericErrorScreenState.INSUFFICIENT_SCOPE -> R.string.tk_credentialOffer_error_insufficientScope_description
        GenericErrorScreenState.INVALID_TOKEN -> R.string.tk_credentialOffer_error_invalidToken_description
        GenericErrorScreenState.UNAUTHORIZED_CLIENT -> R.string.tk_credentialOffer_error_unauthorizedClient_description
        GenericErrorScreenState.UNAUTHORIZED_GRANT_TYPE -> R.string.tk_credentialOffer_error_unauthorizedGrantType_description
    }

    fun onBack() = navManager.navigateBackToHomeScreen(Destination.GenericErrorScreen::class)
}
