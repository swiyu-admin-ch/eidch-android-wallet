package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.DocumentScanOverlay
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.LoadingContent
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.OrientationLocker
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerButton
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerButtonState
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerCamera
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerInfoBox
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentRecording.EIdDocumentRecordingUiState
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentScanner.DocumentScannerErrorContent
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.model.DocumentTypeDrawable
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.model.SDKInfoState
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.addTopScaffoldPadding
import ch.admin.foitt.wallet.platform.composables.presentation.bottomSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.IdentityType
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.LocalActivity
import ch.admin.foitt.wallet.platform.utils.OnPauseEventHandler
import ch.admin.foitt.wallet.platform.utils.OnResumeEventHandler
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun EIdDocumentRecordingScreen(
    viewModel: EIdDocumentRecordingViewModel,
) {
    val currentActivity = LocalActivity.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val documentType by viewModel.documentType.collectAsStateWithLifecycle()
    val shouldLock by viewModel.shouldLock.collectAsStateWithLifecycle()

    val documentTypeDrawables = remember(documentType) {
        when (documentType) {
            IdentityType.SWISS_PASS -> DocumentTypeDrawable(
                front = R.drawable.wallet_passport_front_overlay,
                back = R.drawable.wallet_passport_back_overlay
            )

            else -> DocumentTypeDrawable(
                front = R.drawable.wallet_id_card_front_overlay,
                back = R.drawable.wallet_id_card_back_overlay
            )
        }
    }

    OnResumeEventHandler(viewModel::onResume)
    OnPauseEventHandler(viewModel::onPause)
    BackHandler(enabled = true, viewModel::onUp)

    LaunchedEffect(viewModel) {
        viewModel.initScannerSdk(currentActivity)
    }

    OrientationLocker(currentActivity, shouldLock)

    EIdDocumentRecordingScreenContent(
        uiState = uiState,
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        documentTypeDrawables = documentTypeDrawables,
        getScannerView = viewModel::getScannerView,
        onAfterViewLayout = viewModel::onAfterViewLayout,
        onToggleScan = viewModel::onToggleScan,
    )
}

@Composable
private fun EIdDocumentRecordingScreenContent(
    uiState: EIdDocumentRecordingUiState,
    isLoading: Boolean,
    documentTypeDrawables: DocumentTypeDrawable,
    getScannerView: suspend (width: Int, height: Int) -> View,
    onAfterViewLayout: (width: Int, height: Int) -> Unit,
    onToggleScan: () -> Unit,
) {
    when (uiState) {
        is EIdDocumentRecordingUiState.Error -> DocumentScannerErrorContent(
            onRetry = uiState.onRetry,
            onHelp = uiState.onHelp,
            type = uiState.type,
        )

        EIdDocumentRecordingUiState.Initializing -> LoadingContent()

        is EIdDocumentRecordingUiState.Recording -> {
            EIdDocumentRecordingContent(
                infoState = uiState.infoState,
                infoText = uiState.infoText,
                showSecondSide = uiState.showSecondSide,
                isLoading = isLoading,
                documentTypeDrawables = documentTypeDrawables,
                scannerButtonState = uiState.scannerButtonState,
                getScannerView = getScannerView,
                onAfterViewLayout = onAfterViewLayout,
                onToggleScan = onToggleScan,
            )
        }
    }
}

@Composable
private fun EIdDocumentRecordingContent(
    infoState: SDKInfoState,
    infoText: Int?,
    showSecondSide: Boolean,
    isLoading: Boolean,
    documentTypeDrawables: DocumentTypeDrawable,
    scannerButtonState: ScannerButtonState,
    getScannerView: suspend (width: Int, height: Int) -> View,
    onAfterViewLayout: (width: Int, height: Int) -> Unit,
    onToggleScan: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            ScannerCamera(
                containerWidth = constraints.maxWidth,
                containerHeight = constraints.maxHeight,
                getScannerView = getScannerView,
                onAfterViewLayout = onAfterViewLayout,
            )

            val windowWidthClass = currentWindowAdaptiveInfo().windowWidthClass()
            val isCompact = windowWidthClass == WindowWidthClass.COMPACT

            if (!isLoading && scannerButtonState != ScannerButtonState.Done) {
                DocumentScanOverlay(
                    documentTypeDrawables = documentTypeDrawables,
                    showBackside = showSecondSide,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .then(
                            if (isCompact) {
                                Modifier
                                    .padding(bottom = Sizes.s03)
                                    .navigationBarsPadding()
                            } else {
                                Modifier.padding(top = Sizes.s10)
                            }
                        )
                )
            }

            ScannerButton(
                onClick = onToggleScan,
                state = scannerButtonState,
                modifier = Modifier
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
                    )
                    .then(
                        if (isCompact) {
                            Modifier
                                .bottomSafeDrawing()
                                .align(Alignment.BottomCenter)
                                .padding(bottom = Sizes.s04)
                        } else {
                            Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = Sizes.s04)
                        }
                    )
            )

            ScannerInfoBox(
                infoState = infoState,
                infoText = infoText,
                modifier = Modifier
                    .then(
                        if (isCompact) {
                            Modifier
                                .padding(top = Sizes.s04)
                        } else {
                            Modifier
                        }
                    )
                    .addTopScaffoldPadding()
                    .align(Alignment.TopCenter)
            )

            LoadingOverlay(isLoading)
        }
    }
}

@WalletAllScreenPreview
@Composable
private fun EIdDocumentRecordingScreenContentPreview() {
    WalletTheme {
        var uiState by remember {
            mutableStateOf(
                EIdDocumentRecordingUiState.Recording(
                    infoState = SDKInfoState.InfoData,
                    infoText = R.string.avbeam_error_unknown,
                    scannerButtonState = ScannerButtonState.Ready,
                    showSecondSide = false
                )
            )
        }

        val currentContext = LocalContext.current
        EIdDocumentRecordingScreenContent(
            uiState = uiState,
            isLoading = false,
            documentTypeDrawables = DocumentTypeDrawable(
                front = R.drawable.wallet_id_card_front_overlay,
                back = R.drawable.wallet_id_card_back_overlay
            ),
            getScannerView = { _, _ -> View(currentContext) },
            onAfterViewLayout = { _, _ -> },
            onToggleScan = {
                uiState = uiState.copy(showSecondSide = !uiState.showSecondSide)
            }
        )
    }
}
