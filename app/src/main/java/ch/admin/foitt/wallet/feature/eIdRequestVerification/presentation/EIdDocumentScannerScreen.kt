package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import android.annotation.SuppressLint
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.DocumentScanOverlay
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.LoadingContent
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.OrientationLocker
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerButton
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerButtonAltTexts
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerCamera
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerInfoBox
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentScanner.DocumentScannerBacksideInfoContent
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentScanner.DocumentScannerErrorContent
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentScanner.EIdDocumentScanStatus
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentScanner.EIdDocumentScannerUiState
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.model.DocumentTypeDrawable
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
fun EIdDocumentScannerScreen(
    viewModel: EIdDocumentScannerViewModel,
) {
    val currentActivity = LocalActivity.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shouldLock by viewModel.shouldLock.collectAsStateWithLifecycle()
    val documentType = viewModel.documentType.collectAsStateWithLifecycle().value
    val permissionState by viewModel.permissionState.collectAsStateWithLifecycle()

    OnResumeEventHandler(viewModel::onResumeScan)
    OnPauseEventHandler(viewModel::onPauseScan)
    BackHandler(onBack = viewModel::onUp)

    LaunchedEffect(viewModel) {
        viewModel.initScannerSdk(currentActivity)
    }

    OrientationLocker(currentActivity, shouldLock)

    CameraPermissionWrapper(
        permissionState = permissionState,
        onCameraPermissionResult = viewModel.onPermissionResult,
    ) {
        EIdDocumentScannerScreenContent(
            uiState = uiState,
            documentType = documentType,
            getScannerView = viewModel::getScannerView,
            onAfterViewLayout = viewModel::onAfterViewLayout,
            onToggleScan = viewModel::onToggleScan,
            onContinueToBackside = viewModel::onContinueToBackside,
        )
    }
}

@Composable
private fun EIdDocumentScannerScreenContent(
    uiState: EIdDocumentScannerUiState,
    documentType: EIdUiDocumentType,
    getScannerView: suspend (width: Int, height: Int) -> View,
    onAfterViewLayout: (width: Int, height: Int) -> Unit,
    onToggleScan: () -> Unit,
    onContinueToBackside: () -> Unit,
) {
    when (uiState) {
        is EIdDocumentScannerUiState.Error -> DocumentScannerErrorContent(
            onButton = uiState.onButton,
            onHelp = uiState.onHelp,
            type = uiState.type,
            title = uiState.title,
            content = uiState.content,
            buttonText = uiState.buttonText,
        )

        is EIdDocumentScannerUiState.Initializing -> LoadingContent()

        is EIdDocumentScannerUiState.Scan -> EIdScanContent(
            infoText = uiState.infoText,
            status = uiState.status,
            getScannerView = getScannerView,
            onAfterViewLayout = onAfterViewLayout,
            documentType = documentType,
            onToggleScan = onToggleScan,
            onContinueToBackside = onContinueToBackside,
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EIdScanContent(
    infoText: Int?,
    status: EIdDocumentScanStatus,
    documentType: EIdUiDocumentType,
    getScannerView: suspend (width: Int, height: Int) -> View,
    onAfterViewLayout: (width: Int, height: Int) -> Unit,
    onToggleScan: () -> Unit,
    onContinueToBackside: () -> Unit,
) {
    val windowWidthClass = currentWindowAdaptiveInfo().windowWidthClass()
    val isCompact = windowWidthClass == WindowWidthClass.COMPACT
    val showInfoOverlay = status == EIdDocumentScanStatus.BACKSIDE_INFO

    val documentTypeDrawables by remember(documentType) {
        mutableStateOf(getDocumentTypeDrawables(documentType))
    }

    Box(
        Modifier.fillMaxSize()
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

            if (status != EIdDocumentScanStatus.INITIALIZING && status != EIdDocumentScanStatus.FINISHED) {
                val overlayOrientation = if (isCompact) 90f else 0f

                DocumentScanOverlay(
                    documentTypeDrawables = documentTypeDrawables,
                    showBackside = status.shouldShowOverlayBackside,
                    modifier = Modifier.align(Alignment.Center),
                    cardOrientation = overlayOrientation,
                )
            }

            ScannerButton(
                onClick = onToggleScan,
                state = status.toScannerButtonState(),
                stateTexts = ScannerButtonAltTexts(
                    done = stringResource(R.string.tk_eidRequest_scanDocument_controlButton_done_alt),
                    ready = stringResource(R.string.tk_eidRequest_scanDocument_controlButton_start_alt),
                    scanning = stringResource(R.string.tk_eidRequest_scanDocument_controlButton_stop_alt),
                ),
                modifier = Modifier
                    .focusProperties {
                        canFocus = !showInfoOverlay
                    }
                    .padding(bottom = Sizes.s04)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
                    )
                    .then(
                        if (isCompact) {
                            Modifier
                                .bottomSafeDrawing()
                                .align(Alignment.BottomCenter)
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

        if (showInfoOverlay) {
            // Use as overlay to avoid a new layout of ScannerCamera
            DocumentScannerBacksideInfoContent(
                documentType = documentType,
                onButtonClick = onContinueToBackside,
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

//region Preview
private class DocumentScannerPreviewParamsProvider : PreviewParameterProvider<EIdDocumentScanStatus> {
    override val values = sequenceOf(
        EIdDocumentScanStatus.FRONTSIDE_SCANNING,
        EIdDocumentScanStatus.BACKSIDE_INFO,
        EIdDocumentScanStatus.BACKSIDE,
        EIdDocumentScanStatus.FINISHED,
    )
}

@WalletAllScreenPreview
@Composable
private fun EIdDocumentScannerPreview(
    @PreviewParameter(DocumentScannerPreviewParamsProvider::class) status: EIdDocumentScanStatus,
) {
    WalletTheme {
        var uiState by remember {
            mutableStateOf(
                EIdDocumentScannerUiState.Scan(
                    infoText = R.string.avbeam_error_unknown,
                    status = status,
                )
            )
        }

        val currentContext = LocalContext.current
        EIdDocumentScannerScreenContent(
            uiState = uiState,
            documentType = EIdUiDocumentType.IDENTITY_CARD,
            getScannerView = { _, _ -> View(currentContext) },
            onAfterViewLayout = { _, _ -> },
            onToggleScan = {
                uiState = uiState.copy(
                    status = when (uiState.status) {
                        EIdDocumentScanStatus.INITIALIZING -> EIdDocumentScanStatus.FRONTSIDE
                        EIdDocumentScanStatus.FRONTSIDE -> EIdDocumentScanStatus.FRONTSIDE_SCANNING
                        EIdDocumentScanStatus.FRONTSIDE_SCANNING -> EIdDocumentScanStatus.BACKSIDE_INFO
                        EIdDocumentScanStatus.BACKSIDE_INFO -> EIdDocumentScanStatus.BACKSIDE
                        EIdDocumentScanStatus.BACKSIDE -> EIdDocumentScanStatus.BACKSIDE_SCANNING
                        EIdDocumentScanStatus.BACKSIDE_SCANNING -> EIdDocumentScanStatus.FINISHED
                        EIdDocumentScanStatus.FINISHED -> EIdDocumentScanStatus.INITIALIZING
                    }
                )
            },
            onContinueToBackside = {
                uiState = uiState.copy(
                    status = EIdDocumentScanStatus.BACKSIDE
                )
            },
        )
    }
}
//endregion
