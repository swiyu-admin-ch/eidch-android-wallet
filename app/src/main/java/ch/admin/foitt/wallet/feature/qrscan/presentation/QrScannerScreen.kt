package ch.admin.foitt.wallet.feature.qrscan.presentation

import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.qrscan.domain.model.FlashLightState
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.scaffold.presentation.TopBarBackArrow
import ch.admin.foitt.wallet.platform.scaffold.presentation.TopBarButton
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.theme.FadingVisibility
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme
import ch.admin.foitt.wallet.theme.WalletTopBarColors
import kotlinx.coroutines.delay

@Composable
fun QrScannerScreen(
    viewModel: QrScannerViewModel,
) {
    QrScannerScreenContent(
        flashLightState = viewModel.flashLightState.collectAsStateWithLifecycle().value,
        infoState = viewModel.infoState.collectAsStateWithLifecycle().value,
        scanIsRunning = viewModel.scanIsRunning.collectAsStateWithLifecycle().value,
        onInitScan = viewModel::onInitScan,
        onFlashLightToggle = viewModel::onFlashLight,
        onUp = viewModel::onUp,
        onCloseToast = viewModel::onCloseToast,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QrScannerScreenContent(
    flashLightState: FlashLightState,
    infoState: QrInfoState,
    scanIsRunning: Boolean,
    onInitScan: (PreviewView) -> Unit,
    onFlashLightToggle: () -> Unit,
    onUp: () -> Unit,
    onCloseToast: () -> Unit,
) = Column(
    modifier = Modifier
        .background(
            color = WalletTheme.colorScheme.primaryBackgroundFixed,
        )
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
) {
    TopBarBackArrow(
        titleId = R.string.qrScanner_title,
        showButtonBackground = false,
        onUp = onUp,
        actionButton = {
            FlashLightButton(
                flashLightState = flashLightState,
                onClick = onFlashLightToggle,
            )
        },
        colors = WalletTopBarColors.transparentFixed(),
    )
    Box {
        Camera(
            onInitScan = onInitScan,
        )
        ScanBox(
            scanIsRunning = scanIsRunning,
            modifier = Modifier.align(Alignment.Center)
        )
        InfoBox(
            infoState = infoState,
            onClose = onCloseToast,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun Camera(
    onInitScan: (PreviewView) -> Unit,
) {
    val previewView = remember { mutableStateOf<PreviewView?>(null) }
    AndroidView(
        factory = { androidViewContext -> PreviewView(androidViewContext) },
        modifier = Modifier
            .padding(top = Sizes.s02)
            .fillMaxSize()
            .clip(
                RoundedCornerShape(
                    topStart = Sizes.boxCornerSize,
                    topEnd = Sizes.boxCornerSize,
                )
            )
            .testTag(TestTags.SCANNING_VIEW.name),
        update = { view -> previewView.value = view },
    )
    val firstComposition = rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(previewView.value) {
        previewView.value?.let {
            if (!firstComposition.value) {
                // Sometimes rotating device stops camera because it cannot attach to the surface at this moment
                // delay is only hack found so far
                delay(300)
            } else {
                firstComposition.value = false
            }
            // only init scan once per previewView when previewView is available and attached to lifecycle
            onInitScan(it)
        }
    }
}

@Suppress("CyclomaticComplexMethod")
@Composable
private fun InfoBox(
    infoState: QrInfoState,
    onClose: () -> Unit,
    modifier: Modifier,
) = Box(
    modifier = modifier
        .padding(all = Sizes.s04)
        .navigationBarsPadding()
) {
    FadingVisibility(visible = infoState != QrInfoState.Empty) {
        when (infoState) {
            QrInfoState.Empty -> {}
            QrInfoState.Hint -> QrToastHint(onClose = onClose)
            QrInfoState.InvalidCredentialOffer -> QrToastInvalidCredentialOffer(onClose = onClose)
            QrInfoState.Loading -> LoadingIndicator(modifier)
            QrInfoState.NetworkError -> QrToastNetworkError(onClose = onClose)
            QrInfoState.InvalidPresentation -> QrToastInvalidPresentation(onClose = onClose)
            QrInfoState.ExpiredCredentialOffer -> QrToastExpiredCredentialOffer(onClose = onClose)
            QrInfoState.UnknownIssuer -> QrToastUnknownIssuer(onClose = onClose)
            QrInfoState.UnknownVerifier -> QrToastUnknownVerifier(onClose = onClose)
            QrInfoState.UnexpectedError,
            QrInfoState.InvalidQr -> QrToastInvalidQr(onClose = onClose)
            QrInfoState.UnsupportedKeyStorageSecurityLevel -> QrToastUnsupportedKeyStorageSecurityLevel(onClose = onClose)
            QrInfoState.IncompatibleDeviceKeyStorage -> QrToastIncompatibleDeviceKeyStorage(onClose = onClose)
        }
    }
}

@Composable
private fun ScanBox(
    scanIsRunning: Boolean,
    modifier: Modifier,
) = Box(
    modifier = modifier
        .fillMaxWidth()
        .padding(all = Sizes.s08)
        .navigationBarsPadding(),
    contentAlignment = Alignment.Center,
) {
    FadingVisibility(scanIsRunning) {
        Image(
            painter = painterResource(id = R.drawable.wallet_qrscanner_overlay),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(WalletTheme.colorScheme.onPrimaryFixed),
            modifier = Modifier.sizeIn(
                maxWidth = Sizes.scannerBoxMaxSize,
                maxHeight = Sizes.scannerBoxMaxSize,
            )
        )
    }
}

@Composable
private fun FlashLightButton(
    flashLightState: FlashLightState,
    onClick: () -> Unit,
) = when (flashLightState) {
    FlashLightState.ON -> TopBarButton(
        onClick = onClick,
        icon = R.drawable.wallet_ic_flashlight,
        contentDescription = stringResource(id = R.string.qrScanner_flash_light_button_on),
        iconTint = WalletTheme.colorScheme.primaryBackgroundFixed,
        buttonBackground = WalletTheme.colorScheme.onPrimaryFixed,
    )
    FlashLightState.OFF -> TopBarButton(
        onClick = onClick,
        icon = R.drawable.wallet_ic_flashlight,
        contentDescription = stringResource(id = R.string.qrScanner_flash_light_button_off),
        iconTint = WalletTheme.colorScheme.onPrimaryFixed,
        buttonBackground = null,
    )
    FlashLightState.UNSUPPORTED,
    FlashLightState.UNKNOWN -> {}
}

@Composable
private fun LoadingIndicator(
    modifier: Modifier,
) = Box(
    modifier = modifier
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
        ) {},
    contentAlignment = Alignment.Center,
) {
    CircularProgressIndicator(
        color = WalletTheme.colorScheme.onPrimaryFixed,
        modifier = Modifier
            .padding(bottom = Sizes.s02)
            .size(Sizes.s12),
        strokeWidth = Sizes.line02,
    )
}

private data class QrScannerPreviewParams(
    val flashLightState: FlashLightState,
    val infoState: QrInfoState,
)

private class QrScannerPreviewParamsProvider : PreviewParameterProvider<QrScannerPreviewParams> {
    override val values = sequenceOf(
        QrScannerPreviewParams(
            flashLightState = FlashLightState.ON,
            infoState = QrInfoState.Empty
        ),
        QrScannerPreviewParams(
            flashLightState = FlashLightState.OFF,
            infoState = QrInfoState.Hint
        ),
        QrScannerPreviewParams(
            flashLightState = FlashLightState.UNSUPPORTED,
            infoState = QrInfoState.Loading,
        ),
        QrScannerPreviewParams(
            flashLightState = FlashLightState.OFF,
            infoState = QrInfoState.NetworkError,
        ),
    )
}

@WalletAllScreenPreview
@Composable
private fun QrScannerPreview(
    @PreviewParameter(QrScannerPreviewParamsProvider::class) previewParams: QrScannerPreviewParams,
) {
    WalletTheme {
        QrScannerScreenContent(
            flashLightState = previewParams.flashLightState,
            onInitScan = {},
            onFlashLightToggle = {},
            infoState = previewParams.infoState,
            onUp = {},
            onCloseToast = {},
            scanIsRunning = true,
        )
    }
}
