package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.model

sealed interface EIdPushNotificationUiState {
    data object Initial : EIdPushNotificationUiState

    data object Loading : EIdPushNotificationUiState

    data object SettingsRationale : EIdPushNotificationUiState

    data class Error(
        val onRetry: () -> Unit,
        val onSkip: () -> Unit
    ) : EIdPushNotificationUiState
}
