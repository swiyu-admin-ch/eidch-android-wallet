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
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
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
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerCamera
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerInfoBox
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.model.SDKInfoState
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.scaffold.presentation.TopBarBackArrow
import ch.admin.foitt.wallet.platform.utils.LocalActivity
import ch.admin.foitt.wallet.platform.utils.OnPauseEventHandler
import ch.admin.foitt.wallet.platform.utils.OnResumeEventHandler
import ch.admin.foitt.wallet.platform.utils.lockOrientation
import ch.admin.foitt.wallet.platform.utils.unlockOrientation
import ch.admin.foitt.wallet.theme.WalletTheme
import ch.admin.foitt.wallet.theme.WalletTopBarColors

@Composable
fun EIdFaceScannerScreen(
    viewModel: EIdFaceScannerViewModel,
) {
    val currentActivity = LocalActivity.current
    OnResumeEventHandler(viewModel::onResume)
    OnPauseEventHandler(viewModel::onPause)
    BackHandler(enabled = true, viewModel::onUp)

    LaunchedEffect(viewModel) {
        viewModel.initScannerSdk(currentActivity)
    }

    val shouldLock by viewModel.lockOrientation.collectAsStateWithLifecycle()

    LaunchedEffect(shouldLock) {
        currentActivity.let {
            if (shouldLock) {
                it.lockOrientation()
            } else {
                it.unlockOrientation()
            }
        }
    }

    EIdFaceScannerScreenContent(
        infoState = viewModel.infoState.collectAsStateWithLifecycle().value,
        infoText = viewModel.infoText.collectAsStateWithLifecycle().value,
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        getScannerView = viewModel::getScannerView,
        onAfterViewLayout = viewModel::onAfterViewLayout,
        onUp = viewModel::onUp,
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EIdFaceScannerScreenContent(
    infoState: SDKInfoState,
    infoText: Int?,
    isLoading: Boolean,
    getScannerView: suspend (width: Int, height: Int) -> View,
    onAfterViewLayout: (width: Int, height: Int) -> Unit,
    onUp: () -> Unit,
) = Column(
    modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
) {
    TopBarBackArrow(
        titleId = null,
        showButtonBackground = false,
        onUp = onUp,
        actionButton = {},
        colors = WalletTopBarColors.transparent(),
    )
    BoxWithConstraints {
        ScannerCamera(
            containerWidth = constraints.maxWidth,
            containerHeight = constraints.maxHeight,
            getScannerView = getScannerView,
            onAfterViewLayout = onAfterViewLayout,
        )

        if (!isLoading) {
            ScanBox(
                modifier = Modifier.align(Alignment.Center)
            )
            ScannerInfoBox(
                infoState = infoState,
                infoText = infoText,
                modifier = Modifier
                    .align(Alignment.TopCenter)
            )
        }
    }
    LoadingOverlay(isLoading)
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
                .sizeIn(maxWidth = 1200.dp, maxHeight = 1200.dp)
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
            infoState = previewParams.infoState,
            infoText = R.string.avbeam_error_unknown,
            isLoading = false,
            getScannerView = { _, _ -> View(currentContext) },
            onAfterViewLayout = { _, _ -> },
            onUp = {},
        )
    }
}
