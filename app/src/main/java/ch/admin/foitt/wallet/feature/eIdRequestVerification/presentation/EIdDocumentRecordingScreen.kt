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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.DocumentScanOverlay
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.LoadingContent
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.OrientationLocker
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerButton
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerButtonAltTexts
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerCamera
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerInfoBox
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentRecording.EIdDocumentRecordingStatus
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentRecording.EIdDocumentRecordingUiState
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentScanner.DocumentScannerErrorContent
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.model.DocumentTypeDrawable
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.model.OnPermissionResult
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.model.PermissionState
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.presentation.CameraPermissionWrapper
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.addTopScaffoldPadding
import ch.admin.foitt.wallet.platform.composables.presentation.bottomSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdUiDocumentType
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.LocalActivity
import ch.admin.foitt.wallet.platform.utils.OnPauseEventHandler
import ch.admin.foitt.wallet.platform.utils.OnResumeEventHandler
import ch.admin.foitt.wallet.platform.utils.TraversalIndex
import ch.admin.foitt.wallet.platform.utils.setIsTraversalGroup
import ch.admin.foitt.wallet.platform.utils.traversalIndex
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
    val permissionState by viewModel.permissionState.collectAsStateWithLifecycle()

    OnResumeEventHandler(viewModel::onResume)
    OnPauseEventHandler(viewModel::onPause)
    BackHandler(onBack = viewModel::onUp)

    LaunchedEffect(viewModel) {
        viewModel.initScannerSdk(currentActivity)
    }

    OrientationLocker(currentActivity, shouldLock)

    EIdDocumentRecordingScreenContent(
        uiState = uiState,
        permissionState = permissionState,
        documentType = documentType,
        getScannerView = viewModel::getScannerView,
        onAfterViewLayout = viewModel::onAfterViewLayout,
        onToggleScan = viewModel::onToggleScan,
        onPermissionResult = viewModel.onPermissionResult,
    )
}

@Composable
private fun EIdDocumentRecordingScreenContent(
    uiState: EIdDocumentRecordingUiState,
    permissionState: PermissionState,
    documentType: EIdUiDocumentType,
    getScannerView: suspend (width: Int, height: Int) -> View,
    onAfterViewLayout: (width: Int, height: Int) -> Unit,
    onToggleScan: () -> Unit,
    onPermissionResult: OnPermissionResult,
) = CameraPermissionWrapper(
    permissionState = permissionState,
    onCameraPermissionResult = onPermissionResult,
) {
    when (uiState) {
        is EIdDocumentRecordingUiState.Error -> DocumentScannerErrorContent(
            onButton = uiState.onRetry,
            onHelp = uiState.onHelp,
            type = uiState.type,
            title = R.string.tk_error_generic_primary,
            content = R.string.tk_error_generic_secondary,
            buttonText = R.string.tk_error_generic_button_primary,
        )

        EIdDocumentRecordingUiState.Initializing -> LoadingContent()

        is EIdDocumentRecordingUiState.Recording -> {
            EIdDocumentRecordingContent(
                infoText = uiState.infoText,
                status = uiState.status,
                documentType = documentType,
                getScannerView = getScannerView,
                onAfterViewLayout = onAfterViewLayout,
                onToggleScan = onToggleScan,
            )
        }
    }
}

@Composable
private fun EIdDocumentRecordingContent(
    infoText: Int?,
    status: EIdDocumentRecordingStatus,
    documentType: EIdUiDocumentType,
    getScannerView: suspend (width: Int, height: Int) -> View,
    onAfterViewLayout: (width: Int, height: Int) -> Unit,
    onToggleScan: () -> Unit,
) {
    val documentTypeDrawables by remember(documentType) {
        mutableStateOf(getDocumentTypeDrawables(documentType))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .setIsTraversalGroup()
        ) {
            ScannerCamera(
                containerWidth = constraints.maxWidth,
                containerHeight = constraints.maxHeight,
                getScannerView = getScannerView,
                onAfterViewLayout = onAfterViewLayout,
            )

            val windowWidthClass = currentWindowAdaptiveInfo().windowWidthClass()
            val isCompact = windowWidthClass == WindowWidthClass.COMPACT

            if (status != EIdDocumentRecordingStatus.Initializing && status != EIdDocumentRecordingStatus.Finished) {
                DocumentScanOverlay(
                    documentTypeDrawables = documentTypeDrawables,
                    showBackside = status.shouldShowOverlayBackside,
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
                state = status.toScannerButtonState(),
                stateTexts = ScannerButtonAltTexts(
                    done = stringResource(R.string.tk_eidRequest_recordDocument_controlButton_done_alt),
                    ready = stringResource(
                        R.string.tk_eidRequest_recordDocument_controlButton_start_alt,
                        EIdDocumentRecordingViewModel.VIDEO_LENGTH_SECONDS,
                    ),
                    scanning = stringResource(R.string.tk_eidRequest_recordDocument_controlButton_stop_alt),
                ),
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
                infoText = infoText,
                modifier = Modifier
                    .traversalIndex(TraversalIndex.LAST)
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
        }
    }
}

private fun getDocumentTypeDrawables(documentType: EIdUiDocumentType) = when (documentType) {
    EIdUiDocumentType.PASSPORT -> DocumentTypeDrawable(
        front = R.drawable.wallet_passport_front_overlay,
        back = R.drawable.wallet_passport_back_overlay
    )

    EIdUiDocumentType.IDENTITY_CARD,
    EIdUiDocumentType.RESIDENT_PERMIT -> DocumentTypeDrawable(
        front = R.drawable.wallet_id_card_front_overlay,
        back = R.drawable.wallet_id_card_back_overlay
    )
}

@WalletAllScreenPreview
@Composable
private fun EIdDocumentRecordingScreenContentPreview() {
    WalletTheme {
        var uiState by remember {
            mutableStateOf(
                EIdDocumentRecordingUiState.Recording(
                    infoText = R.string.avbeam_error_unknown,
                    status = EIdDocumentRecordingStatus.FrontSide,
                )
            )
        }

        val currentContext = LocalContext.current
        EIdDocumentRecordingScreenContent(
            uiState = uiState,
            permissionState = PermissionState.Granted,
            documentType = EIdUiDocumentType.PASSPORT,
            getScannerView = { _, _ -> View(currentContext) },
            onAfterViewLayout = { _, _ -> },
            onToggleScan = {},
            onPermissionResult = { _, _, _ -> },
        )
    }
}
