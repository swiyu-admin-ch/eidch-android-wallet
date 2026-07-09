package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import android.annotation.SuppressLint
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.LoadingContent
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.OrientationLocker
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerButton
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerButtonAltTexts
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerCamera
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerInfoBox
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.faceScanner.EIdFaceScanStatus
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.faceScanner.EIdFaceScannerUiState
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.faceScanner.FaceScannerErrorContent
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.presentation.CameraPermissionWrapper
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.addTopScaffoldPadding
import ch.admin.foitt.wallet.platform.composables.presentation.bottomSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
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
fun EIdFaceScannerScreen(
    viewModel: EIdFaceScannerViewModel,
) {
    val currentActivity = LocalActivity.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shouldLock by viewModel.shouldLock.collectAsStateWithLifecycle()
    val permissionState by viewModel.permissionState.collectAsStateWithLifecycle()

    OnResumeEventHandler(viewModel::onResume)
    OnPauseEventHandler(viewModel::onPause)
    BackHandler(enabled = true, viewModel::onUp)

    LaunchedEffect(viewModel) {
        viewModel.initScannerSdk(currentActivity)
    }

    OrientationLocker(currentActivity, shouldLock)

    CameraPermissionWrapper(
        permissionState = permissionState,
        onCameraPermissionResult = viewModel.onPermissionResult,
    ) {
        EIdFaceScannerScreenContent(
            uiState = uiState,
            getScannerView = viewModel::getScannerView,
            onAfterViewLayout = viewModel::onAfterViewLayout,
            onToggleScan = viewModel::onToggleScan,
        )
    }
}

@Composable
private fun EIdFaceScannerScreenContent(
    uiState: EIdFaceScannerUiState,
    getScannerView: suspend (width: Int, height: Int) -> View,
    onAfterViewLayout: (width: Int, height: Int) -> Unit,
    onToggleScan: () -> Unit,
) {
    when (uiState) {
        is EIdFaceScannerUiState.Error -> FaceScannerErrorContent(
            title = uiState.title,
            content = uiState.content,
            buttonText = uiState.buttonText,
            onButton = uiState.onButton,
            onHelp = uiState.onHelp,
        )

        EIdFaceScannerUiState.Initializing -> LoadingContent()

        is EIdFaceScannerUiState.Scanning -> {
            EIdFaceScannerScannerContent(
                infoText = uiState.infoText,
                status = uiState.status,
                getScannerView = getScannerView,
                onAfterViewLayout = onAfterViewLayout,
                onToggleScan = onToggleScan,
            )
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EIdFaceScannerScannerContent(
    infoText: Int?,
    status: EIdFaceScanStatus,
    getScannerView: suspend (width: Int, height: Int) -> View,
    onAfterViewLayout: (width: Int, height: Int) -> Unit,
    onToggleScan: () -> Unit,
) = Box(
    modifier = Modifier.fillMaxSize()
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

        if (status != EIdFaceScanStatus.Initializing && status != EIdFaceScanStatus.Finished) {
            ScanBox(
                modifier = Modifier
                    .align(Alignment.Center)
                    .then(
                        if (isCompact) {
                            Modifier
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
                done = stringResource(R.string.tk_eidRequest_scanFace_controlButton_done_alt),
                ready = stringResource(
                    R.string.tk_eidRequest_scanFace_controlButton_start_alt,
                    EIdFaceScannerViewModel.VIDEO_LENGTH_SECONDS,
                ),
                scanning = stringResource(R.string.tk_eidRequest_scanFace_controlButton_stop_alt),
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

@Composable
private fun ScanBox(
    modifier: Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.wallet_facescanner_overlay),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(WalletTheme.colorScheme.onPrimaryFixed),
            modifier = Modifier
                .sizeIn(maxWidth = 1000.dp, maxHeight = 1000.dp)
        )
    }
}

private class FaceScannerPreviewParamsProvider : PreviewParameterProvider<EIdFaceScanStatus> {
    override val values = sequenceOf(
        EIdFaceScanStatus.Scanning(0.33f),
        EIdFaceScanStatus.Finished,
    )
}

@WalletAllScreenPreview
@Composable
private fun EIdFaceScannerScreenPreview(
    @PreviewParameter(FaceScannerPreviewParamsProvider::class) status: EIdFaceScanStatus,
) {
    WalletTheme {
        val currentContext = LocalContext.current
        EIdFaceScannerScreenContent(
            uiState = EIdFaceScannerUiState.Scanning(
                infoText = R.string.avbeam_error_unknown,
                status = status,
            ),
            getScannerView = { _, _ -> View(currentContext) },
            onAfterViewLayout = { _, _ -> },
            onToggleScan = {},
        )
    }
}
