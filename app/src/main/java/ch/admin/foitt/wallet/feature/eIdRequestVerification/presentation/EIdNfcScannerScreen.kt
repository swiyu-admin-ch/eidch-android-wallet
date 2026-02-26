package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.nfcScanner.EIdNfcScannerUiState
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.nfcScanner.NfcScannerChipDataReadingContent
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.nfcScanner.NfcScannerChipDetectionContent
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.nfcScanner.NfcScannerErrorContent
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.nfcScanner.NfcScannerInfoContent
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.nfcScanner.NfcScannerLoadingContent
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.nfcScanner.NfcScannerNfcDisabledContent
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.nfcScanner.NfcScannerSuccessContent
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.LocalActivity
import ch.admin.foitt.wallet.platform.utils.LocalIntent
import ch.admin.foitt.wallet.platform.utils.OnLifecycleEventHandler
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun EIdNfcScannerScreen(viewModel: EIdNfcScannerViewModel) {
    val currentActivity = LocalActivity.current
    val currentIntent = LocalIntent.current
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    OnLifecycleEventHandler { viewModel.onLifecycleEvent(it, currentActivity) }
    BackHandler { viewModel.onBack() }

    LaunchedEffect(currentIntent) {
        viewModel.onNewIntent(currentIntent)
    }

    EIdNfcScannerScreenContent(
        uiState = uiState,
    )
}

@Composable
private fun EIdNfcScannerScreenContent(
    uiState: EIdNfcScannerUiState,
) = when (uiState) {
    is EIdNfcScannerUiState.Error -> NfcScannerErrorContent(
        onRetry = uiState.onRetry,
    )
    is EIdNfcScannerUiState.Info -> NfcScannerInfoContent(
        onStart = uiState.onStart,
        onTips = uiState.onTips,
    )
    is EIdNfcScannerUiState.Initializing -> NfcScannerLoadingContent()
    is EIdNfcScannerUiState.Scanning -> NfcScannerChipDetectionContent(
        onStop = uiState.onStop,
    )
    is EIdNfcScannerUiState.ReadingChipData -> NfcScannerChipDataReadingContent(
        onStop = uiState.onStop,
    )
    is EIdNfcScannerUiState.Success -> NfcScannerSuccessContent()
    is EIdNfcScannerUiState.NfcDisabled -> NfcScannerNfcDisabledContent(
        onEnableNfc = uiState.onEnable,
    )
}

@Composable
@WalletAllScreenPreview
private fun EIdNfcScannerPreview() {
    WalletTheme {
        EIdNfcScannerScreenContent(
            uiState = EIdNfcScannerUiState.Info(
                onStart = {},
                onTips = {},
            )
        )
    }
}
