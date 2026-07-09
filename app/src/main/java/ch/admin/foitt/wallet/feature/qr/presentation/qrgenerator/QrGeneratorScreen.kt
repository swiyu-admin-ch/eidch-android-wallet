package ch.admin.foitt.wallet.feature.qr.presentation.qrgenerator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.QrCodeImage
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.nonFocusableAccessibilityAnchor
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.permission.presentation.bluetooth.BluetoothStateScaffold
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.scaffold.presentation.LocalScaffoldPaddings
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun QrGeneratorScreen(
    viewModel: QrGeneratorViewModel,
    updateContentShown: (Boolean) -> Unit,
    pagerControlHeight: MutableState<Dp>,
) {
    val context = LocalContext.current

    BluetoothStateScaffold(
        bluetoothState = viewModel.bluetoothState.collectAsStateWithLifecycle(),
        onBluetoothStateChanged = { viewModel.updateBluetoothState(context) },
        updateContentShown = updateContentShown
    ) {
        QrGeneratorScreenContent(
            pagerControlHeight,
            viewModel.qrCodePayload.collectAsStateWithLifecycle(),
        )
    }
}

@Composable
private fun QrGeneratorScreenContent(pagerControlHeight: MutableState<Dp>, qrCode: State<String?>) {
    BoxWithConstraints(
        modifier = Modifier.background(WalletTheme.colorScheme.surfaceContainerLow)
    ) {
        val qrCodeModifier = if (maxHeight < maxWidth) Modifier.heightIn(max = maxHeight) else Modifier.widthIn(max = maxWidth)

        when (currentWindowAdaptiveInfo().windowWidthClass()) {
            WindowWidthClass.COMPACT -> WalletLayouts.CompactContainer(
                scaffoldPaddings = LocalScaffoldPaddings.current,
                scrollState = rememberScrollState(),
                onBottomHeightMeasured = null,
                stickyBottomContent = {},
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        color = WalletTheme.colorScheme.onWhiteTransparentFixed,
                        shape = RoundedCornerShape(Sizes.s04),
                        modifier = qrCodeModifier
                            .padding(Sizes.s04)
                            .aspectRatio(1f)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Box {
                            QrCodeImage(
                                content = qrCode.value,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .fillMaxSize(0.8f)
                            )
                        }
                    }
                    WalletTexts.BodyLargeEmphasized(
                        modifier = Modifier
                            .nonFocusableAccessibilityAnchor()
                            .padding(start = Sizes.s04, top = Sizes.s06, end = Sizes.s04),
                        text = stringResource(R.string.qr_generator_title),
                        textAlign = TextAlign.Center,
                    )
                    WalletTexts.BodyLarge(
                        modifier = Modifier
                            .nonFocusableAccessibilityAnchor()
                            .padding(start = Sizes.s04, bottom = Sizes.s06 + pagerControlHeight.value, end = Sizes.s04),
                        text = stringResource(R.string.qr_generator_message),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            else -> WalletLayouts.LargeContainer(
                scaffoldPaddings = LocalScaffoldPaddings.current,
                onBottomHeightMeasured = {},
                isStickyStartScrollable = false,
                stickyBottomContent = {},
                stickyStartPadding = PaddingValues(Sizes.s04),
                stickyStartContent = {
                    Surface(
                        color = WalletTheme.colorScheme.onWhiteTransparentFixed,
                        shape = RoundedCornerShape(Sizes.s04),
                        modifier = qrCodeModifier.aspectRatio(1f)
                    ) {
                        Box {
                            QrCodeImage(
                                content = qrCode.value,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .fillMaxSize(0.8f)
                            )
                        }
                    }
                },
                contentPadding = PaddingValues(),
            ) {
                WalletTexts.BodyLargeEmphasized(
                    modifier = Modifier
                        .nonFocusableAccessibilityAnchor()
                        .padding(start = Sizes.s04, top = Sizes.s06, end = Sizes.s04)
                        .fillMaxWidth(),
                    text = stringResource(R.string.qr_generator_title),
                    textAlign = TextAlign.Center,
                )
                WalletTexts.BodyLarge(
                    modifier = Modifier
                        .nonFocusableAccessibilityAnchor()
                        .padding(start = Sizes.s04, bottom = Sizes.s06 + pagerControlHeight.value, end = Sizes.s04)
                        .fillMaxWidth(),
                    text = stringResource(R.string.qr_generator_message),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@WalletAllScreenPreview
@Composable
private fun QrGeneratorScreenPreview() {
    WalletTheme {
        QrGeneratorScreenContent(
            remember { mutableStateOf(0.dp) },
            qrCode = remember { mutableStateOf("Test") },
        )
    }
}
