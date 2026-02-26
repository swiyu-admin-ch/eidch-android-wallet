package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import android.annotation.SuppressLint
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.OrientationLocker
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerButton
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerButtonState
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerCamera
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerInfoBox
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.model.DocumentTypeDrawable
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.model.SDKInfoState
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdDocumentType
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.scaffold.presentation.TopBarBackArrow
import ch.admin.foitt.wallet.platform.utils.LocalActivity
import ch.admin.foitt.wallet.platform.utils.OnPauseEventHandler
import ch.admin.foitt.wallet.platform.utils.OnResumeEventHandler
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme
import ch.admin.foitt.wallet.theme.WalletTopBarColors

@Composable
fun EIdDocumentScannerScreen(
    viewModel: EIdDocumentScannerViewModel,
) {
    val currentActivity = LocalActivity.current

    val documentTypeDrawables = when (viewModel.documentType) {
        EIdDocumentType.PASSPORT -> DocumentTypeDrawable(
            front = R.drawable.wallet_passport_front_overlay,
            back = R.drawable.wallet_passport_back_overlay
        )

        else -> DocumentTypeDrawable(
            front = R.drawable.wallet_id_card_front_overlay,
            back = R.drawable.wallet_id_card_back_overlay
        )
    }

    OrientationLocker()

    OnResumeEventHandler(viewModel::onResume)
    OnPauseEventHandler(viewModel::onPause)
    BackHandler(enabled = true, viewModel::onUp)

    LaunchedEffect(viewModel) {
        viewModel.initScannerSdk(currentActivity)
    }

    EIdDocumentScannerScreenContent(
        infoState = viewModel.infoState.collectAsStateWithLifecycle().value,
        infoText = viewModel.infoText.collectAsStateWithLifecycle().value,
        showSecondSide = viewModel.changeToBackCard.collectAsStateWithLifecycle().value,
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        scannerButtonState = viewModel.scannerButtonState.collectAsStateWithLifecycle().value,
        getScannerView = viewModel::getScannerView,
        onAfterViewLayout = viewModel::onAfterViewLayout,
        documentTypeDrawables = documentTypeDrawables,
        onUp = viewModel::onUp,
        onToggleScan = viewModel::onToggleScan,
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EIdDocumentScannerScreenContent(
    infoState: SDKInfoState,
    infoText: Int?,
    showSecondSide: Boolean,
    isLoading: Boolean,
    scannerButtonState: ScannerButtonState,
    getScannerView: suspend (width: Int, height: Int) -> View,
    onAfterViewLayout: (width: Int, height: Int) -> Unit,
    documentTypeDrawables: DocumentTypeDrawable,
    onUp: () -> Unit,
    onToggleScan: () -> Unit,
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

        if (!isLoading && scannerButtonState != ScannerButtonState.Done) {
            ScanBox(
                changeToBackCard = showSecondSide,
                modifier = Modifier.align(Alignment.Center),
                documentTypeDrawables = documentTypeDrawables
            )
        }
        ScannerButton(
            onClick = onToggleScan,
            state = scannerButtonState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = Sizes.s04)
        )
        ScannerInfoBox(
            infoState = infoState,
            infoText = infoText,
            modifier = Modifier
                .align(Alignment.TopCenter)
        )
        LoadingOverlay(isLoading)
    }
}

@Composable
private fun ScanBox(
    changeToBackCard: Boolean,
    modifier: Modifier,
    documentTypeDrawables: DocumentTypeDrawable,
) {
    val rotation = remember { Animatable(0f) }
    var currentCardFront by remember { mutableStateOf(true) }
    val cardOrientation = when (currentWindowAdaptiveInfo().windowWidthClass()) {
        WindowWidthClass.COMPACT -> 90f
        else -> 0f
    }

    LaunchedEffect(changeToBackCard) {
        if (changeToBackCard && currentCardFront) {
            rotation.animateTo(
                targetValue = 90f,
                animationSpec = tween(durationMillis = 250, easing = LinearEasing)
            )
            currentCardFront = false
            rotation.animateTo(
                targetValue = 180f,
                animationSpec = tween(durationMillis = 250, easing = LinearEasing)
            )
            rotation.snapTo(targetValue = 0f)
        } else {
            currentCardFront = true
        }
    }

    val overlayRes = if (changeToBackCard) {
        documentTypeDrawables.back
    } else {
        documentTypeDrawables.front
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = overlayRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(WalletTheme.colorScheme.onPrimaryFixed),
            modifier = Modifier
                .rotate(cardOrientation)
                .sizeIn(maxWidth = 1200.dp, maxHeight = 1200.dp)
                .graphicsLayer {
                    rotationY = rotation.value
                    cameraDistance = 8 * density
                }
        )
    }
}

private data class DocumentScannerPreviewParams(
    val infoState: SDKInfoState,
)

private class DocumentScannerPreviewParamsProvider : PreviewParameterProvider<DocumentScannerPreviewParams> {
    override val values = sequenceOf(
        DocumentScannerPreviewParams(
            infoState = SDKInfoState.InfoData
        ),
    )
}

@WalletAllScreenPreview
@Composable
private fun EIdDocumentScannerPreview(
    @PreviewParameter(DocumentScannerPreviewParamsProvider::class) previewParams: DocumentScannerPreviewParams,
) {
    WalletTheme {
        val currentContext = LocalContext.current
        EIdDocumentScannerScreenContent(
            infoState = previewParams.infoState,
            infoText = R.string.avbeam_error_unknown,
            scannerButtonState = ScannerButtonState.Ready,
            showSecondSide = false,
            isLoading = false,
            getScannerView = { _, _ -> View(currentContext) },
            onAfterViewLayout = { _, _ -> },
            onUp = {},
            onToggleScan = {},
            documentTypeDrawables = DocumentTypeDrawable(
                front = R.drawable.wallet_id_card_front_overlay,
                back = R.drawable.wallet_id_card_back_overlay
            )
        )
    }
}
