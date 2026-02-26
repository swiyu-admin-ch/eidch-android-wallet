package ch.admin.foitt.wallet.feature.home.presentation.model

import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SIdRequestDisplayData

sealed interface HomeScreenState {
    data object Initial : HomeScreenState
    data class CredentialList(
        val eIdRequests: List<SIdRequestDisplayData>,
        val credentials: List<CredentialCardState>,
        val onCredentialClick: (Long, VerifiableProgressionState) -> Unit,
    ) : HomeScreenState
    data class NoCredential(
        val eIdRequests: List<SIdRequestDisplayData>,
    ) : HomeScreenState
    data object WalletEmpty : HomeScreenState
    data object UnexpectedError : HomeScreenState
}
