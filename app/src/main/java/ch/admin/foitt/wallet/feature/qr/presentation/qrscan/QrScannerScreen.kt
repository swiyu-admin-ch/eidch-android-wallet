package ch.admin.foitt.wallet.feature.qr.presentation.qrscan

import androidx.activity.enableEdgeToEdge
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.qr.domain.model.qrscan.FlashLightState
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.bottomSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.permission.presentation.bluetooth.BluetoothStateScaffold
import ch.admin.foitt.wallet.platform.permission.presentation.camera.CameraStateScaffold
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.scaffold.presentation.LocalScaffoldPaddings
import ch.admin.foitt.wallet.platform.scaffold.presentation.TopBarBackArrow
import ch.admin.foitt.wallet.platform.scaffold.presentation.TopBarRoundButton
import ch.admin.foitt.wallet.platform.utils.LocalActivity
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.theme.FadingVisibility
import ch.admin.foitt.wallet.theme.LocalIsInDarkTheme
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme
import ch.admin.foitt.wallet.theme.WalletTopBarColors
import kotlinx.coroutines.delay

@Composable
fun QrScannerScreen(
    viewModel: QrScannerViewModel,
    updateContentShown: (Boolean) -> Unit,
) {
    val context = LocalContext.current

    val currentActivity = LocalActivity.current
    val isInDarkTheme = LocalIsInDarkTheme.current
    val isCameraRunning = viewModel.isCameraRunning.collectAsStateWithLifecycle()
    LaunchedEffect(isCameraRunning.value) {
        viewModel.systemBarsFixedLightColor = isCameraRunning.value
        viewModel.syncScaffoldState(
            currentActivity::enableEdgeToEdge,
            isInDarkTheme
        )
        viewModel.initProximity()
    }

    if (viewModel.isProximityEngagementEnabled) {
        BluetoothStateScaffold(
            bluetoothState = viewModel.bluetoothState.collectAsStateWithLifecycle(),
            onBluetoothStateChanged = { viewModel.updateBluetoothState(context) },
            updateContentShown = updateContentShown
        ) {
            CameraStateScaffoldWrappedQrScannerScreenContent(viewModel, updateContentShown)
        }
    } else {
        CameraStateScaffoldWrappedQrScannerScreenContent(viewModel, updateContentShown)
    }
}

@Composable
private fun CameraStateScaffoldWrappedQrScannerScreenContent(
    viewModel: QrScannerViewModel,
    updateContentShown: (Boolean) -> Unit
) {
    CameraStateScaffold(
        onCameraPermissionChanged = {
            viewModel.onCameraStateChange()
        },
        updateContentShown = updateContentShown
    ) {
        QrScannerScreenContent(
            isProximityEngagementEnabled = viewModel.isProximityEngagementEnabled,
            flashLightState = viewModel.flashLightState.collectAsStateWithLifecycle().value,
            infoState = viewModel.infoState.collectAsStateWithLifecycle().value,
            scanIsRunning = viewModel.scanIsRunning.collectAsStateWithLifecycle().value,
            onInitScan = viewModel::onInitScan,
            onFlashLightToggle = viewModel::onFlashLight,
            onUp = viewModel::onUp,
            onCloseToast = viewModel::onCloseToast,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QrScannerScreenContent(
    isProximityEngagementEnabled: Boolean,
    flashLightState: FlashLightState,
    infoState: QrInfoState,
    scanIsRunning: Boolean,
    onInitScan: (PreviewView) -> Unit,
    onFlashLightToggle: () -> Unit,
    onUp: () -> Unit,
    onCloseToast: () -> Unit,
) = Box(modifier = Modifier.fillMaxSize()) {
    Camera(
        onInitScan = onInitScan,
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val hintPadding = if (isProximityEngagementEnabled) 140.dp else 0.dp
        Box {
            when (currentWindowAdaptiveInfo().windowWidthClass()) {
                WindowWidthClass.COMPACT -> Box {
                    Box(modifier = Modifier.fillMaxSize()) {
                        ScanBox(
                            scanIsRunning = scanIsRunning,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(bottom = hintPadding)
                            .align(Alignment.BottomCenter),
                    ) {
                        InfoBox(
                            infoState = infoState,
                            onClose = onCloseToast,
                        )
                    }
                }

                else -> WalletLayouts.LargeContainer(
                    scaffoldPaddings = LocalScaffoldPaddings.current,
                    onBottomHeightMeasured = {},
                    isStickyStartScrollable = false,
                    stickyBottomContent = {
                        Box(
                            modifier = Modifier
                                .padding(bottom = hintPadding)
                                .padding(vertical = Sizes.s02, horizontal = Sizes.s04)
                                .bottomSafeDrawing(),
                        ) {
                            InfoBox(
                                infoState = infoState,
                                onClose = onCloseToast,
                            )
                        }
                    },
                    stickyStartPadding = PaddingValues(Sizes.s04),
                    stickyStartContent = {
                        Box(modifier = Modifier.fillMaxSize()) {
                            ScanBox(
                                scanIsRunning = scanIsRunning,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    },
                    contentPadding = PaddingValues(),
                    modifier = Modifier.navigationBarsPadding()
                ) {}
            }
        }
    }
    TopBarBackArrow(
        titleId = R.string.qrScanner_title,
        showButtonBackground = true,
        onUp = onUp,
        actionButton = {
            FlashLightButton(
                flashLightState = flashLightState,
                onClick = onFlashLightToggle,
            )
        },
        backgroundGradient = Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)),
        colors = WalletTopBarColors.transparentFixed(),
    )
}

@Composable
private fun Camera(
    onInitScan: (PreviewView) -> Unit,
) {
    val previewView = remember { mutableStateOf<PreviewView?>(null) }
    AndroidView(
        factory = { androidViewContext -> PreviewView(androidViewContext) },
        modifier = Modifier
            .fillMaxSize()
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
    modifier: Modifier = Modifier,
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
    FlashLightState.ON -> TopBarRoundButton(
        onClick = onClick,
        icon = R.drawable.wallet_ic_flashlight,
        contentDescription = stringResource(id = R.string.qrScanner_flash_light_button_on),
        iconTint = WalletTheme.colorScheme.primaryBackgroundFixed,
        backgroundColors = IconButtonColors(
            containerColor = WalletTheme.colorScheme.onPrimaryFixed,
            contentColor = WalletTheme.colorScheme.onPrimaryFixed,
            disabledContainerColor = WalletTheme.colorScheme.onPrimaryFixed,
            disabledContentColor = WalletTheme.colorScheme.onPrimaryFixed
        )
    )

    FlashLightState.OFF -> TopBarRoundButton(
        onClick = onClick,
        icon = R.drawable.wallet_ic_flashlight,
        contentDescription = stringResource(id = R.string.qrScanner_flash_light_button_off),
        iconTint = WalletTheme.colorScheme.onPrimaryFixed,
        backgroundColors = IconButtonColors(
            containerColor = WalletTheme.colorScheme.outline.copy(alpha = 0.24f),
            contentColor = WalletTheme.colorScheme.outline.copy(alpha = 0.24f),
            disabledContainerColor = WalletTheme.colorScheme.outline.copy(alpha = 0.24f),
            disabledContentColor = WalletTheme.colorScheme.outline.copy(alpha = 0.24f),
        )
    )

    FlashLightState.UNSUPPORTED,
    FlashLightState.UNKNOWN -> {
    }
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
            isProximityEngagementEnabled = true,
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
