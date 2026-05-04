package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import android.annotation.SuppressLint
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.LoadingContent
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.OrientationLocker
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerButton
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerButtonState
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerCamera
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerInfoBox
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.faceScanner.EIdFaceScannerUiState
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.faceScanner.FaceScannerErrorContent
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.model.SDKInfoState
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.addTopScaffoldPadding
import ch.admin.foitt.wallet.platform.composables.presentation.bottomSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.LocalActivity
import ch.admin.foitt.wallet.platform.utils.OnPauseEventHandler
import ch.admin.foitt.wallet.platform.utils.OnResumeEventHandler
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun EIdFaceScannerScreen(
    viewModel: EIdFaceScannerViewModel,
) {
    val currentActivity = LocalActivity.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val shouldLock by viewModel.shouldLock.collectAsStateWithLifecycle()

    OnResumeEventHandler(viewModel::onResume)
    OnPauseEventHandler(viewModel::onPause)
    BackHandler(enabled = true, viewModel::onUp)

    LaunchedEffect(viewModel) {
        viewModel.initScannerSdk(currentActivity)
    }

    OrientationLocker(currentActivity, shouldLock)

    EIdFaceScannerScreenContent(
        uiState = uiState,
        isLoading = isLoading,
        getScannerView = viewModel::getScannerView,
        onAfterViewLayout = viewModel::onAfterViewLayout,
        onHelp = viewModel::onHelp,
        onToggleScan = viewModel::onToggleScan
    )
}

@Composable
private fun EIdFaceScannerScreenContent(
    uiState: EIdFaceScannerUiState,
    isLoading: Boolean,
    getScannerView: suspend (width: Int, height: Int) -> View,
    onAfterViewLayout: (width: Int, height: Int) -> Unit,
    onHelp: () -> Unit,
    onToggleScan: () -> Unit,
) = when (uiState) {
    is EIdFaceScannerUiState.Error -> FaceScannerErrorContent(onRetry = uiState.onRetry, onHelp = onHelp)

    EIdFaceScannerUiState.Initializing -> LoadingContent()

    else -> {
        val scanState = uiState as? EIdFaceScannerUiState.Scan
        EIdFaceScannerScannerContent(
            infoState = scanState?.infoState ?: SDKInfoState.Empty,
            infoText = scanState?.infoText,
            isLoading = isLoading,
            scannerButtonState = scanState?.scannerButtonState ?: ScannerButtonState.Ready,
            getScannerView = getScannerView,
            onAfterViewLayout = onAfterViewLayout,
            onToggleScan = onToggleScan,
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EIdFaceScannerScannerContent(
    infoState: SDKInfoState,
    infoText: Int?,
    isLoading: Boolean,
    scannerButtonState: ScannerButtonState,
    getScannerView: suspend (width: Int, height: Int) -> View,
    onAfterViewLayout: (width: Int, height: Int) -> Unit,
    onToggleScan: () -> Unit,
) = Column(
    modifier = Modifier.fillMaxSize()
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
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

@Composable
private fun ScanBox(
    modifier: Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
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

private data class FaceScannerPreviewParams(
    val infoState: SDKInfoState,
)

private class FaceScannerPreviewParamsProvider : PreviewParameterProvider<FaceScannerPreviewParams> {
    override val values = sequenceOf(
        FaceScannerPreviewParams(
            infoState = SDKInfoState.InfoData
        ),
    )
}

@WalletAllScreenPreview
@Composable
private fun EIdFaceScannerScreenPreview(
    @PreviewParameter(FaceScannerPreviewParamsProvider::class) previewParams: FaceScannerPreviewParams,
) {
    WalletTheme {
        val currentContext = LocalContext.current
        EIdFaceScannerScreenContent(
            uiState = EIdFaceScannerUiState.Scan(
                infoState = previewParams.infoState,
                infoText = R.string.avbeam_error_unknown,
                scannerButtonState = ScannerButtonState.Ready,
            ),
            isLoading = false,
            getScannerView = { _, _ -> View(currentContext) },
            onAfterViewLayout = { _, _ -> },
            onHelp = {},
            onToggleScan = {},
        )
    }
}
