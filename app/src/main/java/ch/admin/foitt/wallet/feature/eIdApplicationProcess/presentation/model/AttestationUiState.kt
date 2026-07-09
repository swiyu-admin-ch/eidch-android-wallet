package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.model

internal sealed interface AttestationUiState {
    object Loading : AttestationUiState
    object Valid : AttestationUiState
    data class InvalidKeyAttestation(
        val onClose: () -> Unit,
        val onHelp: () -> Unit,
    ) : AttestationUiState
    data class InvalidClientAttestation(
        val onClose: () -> Unit,
        val onHelp: () -> Unit,
        val onPlaystore: () -> Unit,
    ) : AttestationUiState
    data class Unexpected(
        val onClose: () -> Unit,
        val onRetry: () -> Unit,
    ) : AttestationUiState
    data class NetworkError(
        val onClose: () -> Unit,
        val onRetry: () -> Unit,
    ) : AttestationUiState
    data class TimeoutError(
        val onClose: () -> Unit
    ) : AttestationUiState
    data class IntegrityError(
        val onClose: () -> Unit,
    ) : AttestationUiState
    data class IntegrityNetworkError(
        val onClose: () -> Unit,
        val onRetry: () -> Unit,
    ) : AttestationUiState
}
