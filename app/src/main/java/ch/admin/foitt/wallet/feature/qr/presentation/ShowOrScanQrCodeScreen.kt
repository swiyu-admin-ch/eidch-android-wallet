package ch.admin.foitt.wallet.feature.qr.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.qr.presentation.qrgenerator.QrGeneratorScreen
import ch.admin.foitt.wallet.feature.qr.presentation.qrgenerator.QrGeneratorViewModel
import ch.admin.foitt.wallet.feature.qr.presentation.qrscan.QrScannerScreen
import ch.admin.foitt.wallet.feature.qr.presentation.qrscan.QrScannerViewModel
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.scaffold.presentation.LocalScaffoldPaddings
import ch.admin.foitt.wallet.platform.scaffold.presentation.SyncedScaffoldScreen
import ch.admin.foitt.wallet.platform.verification.domain.model.VerificationMode
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ShowOrScanQrCodeScreen(
    qrGeneratorViewModel: QrGeneratorViewModel,
    qrScannerViewModel: QrScannerViewModel,
    verificationMode: VerificationMode,
    modifier: Modifier = Modifier
) {
    val contentShown = remember { mutableStateOf(false) }
    val updateContentShown = { shown: Boolean -> contentShown.value = shown }

    ShowOrScanQrCodeScreenContent(
        initialPage = if (verificationMode == VerificationMode.SCANNER) 0 else 1,
        contentShown = contentShown.value,
        modifier = modifier,
        onSwitchToQrScanner = {
            qrGeneratorViewModel.reset()
        },
        onSwitchToQrGenerator = {
            qrScannerViewModel.reset()
            qrGeneratorViewModel.startEngagementListener()
        },
        qrScannerContent = {
            SyncedScaffoldScreen(viewModel = qrScannerViewModel) {
                QrScannerScreen(
                    viewModel = qrScannerViewModel,
                    updateContentShown = updateContentShown
                )
            }
        },
        qrGeneratorContent = { pagerControlHeight ->
            SyncedScaffoldScreen(viewModel = qrGeneratorViewModel) {
                QrGeneratorScreen(
                    viewModel = qrGeneratorViewModel,
                    updateContentShown = updateContentShown,
                    pagerControlHeight = pagerControlHeight,
                )
            }
        }
    )
}

@Composable
private fun ShowOrScanQrCodeScreenContent(
    initialPage: Int,
    contentShown: Boolean,
    qrScannerContent: @Composable () -> Unit,
    qrGeneratorContent: @Composable (_: MutableState<Dp>) -> Unit,
    onSwitchToQrScanner: () -> Unit = {},
    onSwitchToQrGenerator: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // Save the current page index across configuration changes
    var savedPageIndex by rememberSaveable {
        mutableIntStateOf(initialPage)
    }

    val pagerState = rememberPagerState(
        initialPage = savedPageIndex,
        pageCount = { 2 }
    )
    val coroutineScope = rememberCoroutineScope()

    // Update the saved page index whenever the pager page changes
    LaunchedEffect(pagerState.currentPage) {
        savedPageIndex = pagerState.currentPage
        when (pagerState.currentPage) {
            0 -> onSwitchToQrScanner()
            1 -> onSwitchToQrGenerator()
        }
    }
    Box(modifier = modifier.fillMaxSize()) {
        val pagerControlHeight = remember { mutableStateOf(0.dp) }
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> qrScannerContent()
                1 -> qrGeneratorContent(pagerControlHeight)
            }
        }

        if (contentShown) {
            val density = LocalDensity.current
            val pagerControlModifier = Modifier.onGloballyPositioned { layoutCoordinates ->
                layoutCoordinates.size.height.let { height ->
                    pagerControlHeight.value = with(density) { height.toDp() }
                }
            }
            when (currentWindowAdaptiveInfo().windowWidthClass()) {
                WindowWidthClass.COMPACT -> {
                    PagerControl(
                        pagerState = pagerState,
                        coroutineScope = coroutineScope,
                        modifier = pagerControlModifier
                    )
                }

                else -> WalletLayouts.LargeContainer(
                    scaffoldPaddings = LocalScaffoldPaddings.current,
                    onBottomHeightMeasured = {},
                    isContentScrollable = false,
                    stickyBottomContent = {
                        PagerControl(
                            pagerState = pagerState,
                            coroutineScope = coroutineScope,
                            modifier = pagerControlModifier
                        )
                    },
                    stickyStartPadding = PaddingValues(Sizes.s04),
                    stickyStartContent = {},
                    contentPadding = PaddingValues(),
                ) {
                }
            }
        }
    }
}

@Composable
private fun BoxScope.PagerControl(
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    SegmentedControl(
        selectedIndex = pagerState.currentPage,
        onSegmentSelected = { index ->
            coroutineScope.launch {
                pagerState.animateScrollToPage(index)
            }
        },
        modifier = modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = Sizes.s11)
    )
}

@Composable
private fun SegmentedControl(
    selectedIndex: Int,
    onSegmentSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = WalletTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(Sizes.s12),
        shadowElevation = 8.dp,
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Sizes.s01),
            modifier = Modifier.padding(Sizes.s03),
        ) {
            SegmentButton(
                text = stringResource(R.string.qr_scanner_scan_tab),
                icon = R.drawable.ic_scanner,
                isSelected = selectedIndex == 0,
                onClick = { onSegmentSelected(0) },
            )
            SegmentButton(
                text = stringResource(R.string.qr_scanner_code_tab),
                icon = R.drawable.ic_qr_code,
                isSelected = selectedIndex == 1,
                onClick = { onSegmentSelected(1) },
            )
        }
    }
}

@Composable
private fun SegmentButton(
    text: String,
    icon: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(vertical = Sizes.s03, horizontal = Sizes.s04),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) WalletTheme.colorScheme.surfaceContainerLowest else Color.Transparent,
            contentColor = if (isSelected) WalletTheme.colorScheme.onSurface else WalletTheme.colorScheme.onSurfaceVariant,
        ),
        shape = RoundedCornerShape(Sizes.s09),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier
                    .size(Sizes.s04),
            )
            Spacer(modifier = Modifier.width(Sizes.s02))
            WalletTexts.BodyLargeEmphasized(
                text = text,
                color = Color.Unspecified
            )
        }
    }
}

@WalletAllScreenPreview
@Composable
private fun ShowOrScanQrCodeScreenPreview() {
    WalletTheme {
        ShowOrScanQrCodeScreenContent(
            initialPage = 0,
            contentShown = true,
            qrScannerContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                )
            },
            qrGeneratorContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                )
            }
        )
    }
}
