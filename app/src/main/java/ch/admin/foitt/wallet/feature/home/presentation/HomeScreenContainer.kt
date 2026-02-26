package ch.admin.foitt.wallet.feature.home.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import ch.admin.foitt.wallet.feature.home.presentation.composables.HomeBarHorizontal
import ch.admin.foitt.wallet.feature.home.presentation.composables.HomeBarVertical
import ch.admin.foitt.wallet.feature.home.presentation.model.HomeContainerState
import ch.admin.foitt.wallet.platform.composables.presentation.HeightReportingLayout
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.bottomSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.endSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.startSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.verticalSafeDrawing
import ch.admin.foitt.wallet.platform.utils.TraversalIndex
import ch.admin.foitt.wallet.platform.utils.setIsTraversalGroup
import ch.admin.foitt.wallet.theme.Sizes

@Composable
fun WalletLayouts.HomeContainer(
    windowWidthClass: WindowWidthClass,
    containerState: HomeContainerState,
    onMenu: (Boolean) -> Unit,
    content: @Composable BoxScope.(stickyBottomHeightDp: Dp) -> Unit,
) = when (windowWidthClass) {
    WindowWidthClass.COMPACT -> HomeCompactContainer(
        containerState = containerState,
        onMenu = onMenu,
        content = content,
    )

    else -> HomeLargeContainer(
        containerState = containerState,
        onMenu = onMenu,
        content = content,
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun HomeCompactContainer(
    modifier: Modifier = Modifier,
    containerState: HomeContainerState,
    onMenu: (Boolean) -> Unit,
    content: @Composable BoxScope.(stickyBottomHeightDp: Dp) -> Unit,
) = ConstraintLayout(
    modifier = modifier.fillMaxSize()
) {
    val (
        contentRef,
        homeBarRef,
        menuRef
    ) = createRefs()

    var reportedBlockHeight by remember {
        mutableStateOf(0.dp)
    }

    var usesBigScanButton by remember {
        mutableStateOf(true)
    }

    Box(
        modifier = Modifier.constrainAs(contentRef) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom)
        }
    ) {
        content(reportedBlockHeight)
    }

    HomeCompactStickyBottom(
        modifier = Modifier.constrainAs(homeBarRef) {
            bottom.linkTo(parent.bottom)
            start.linkTo(anchor = parent.start, margin = Sizes.s14)
            end.linkTo(anchor = parent.end, margin = Sizes.s14)
            width = Dimension.fillToConstraints
        },
        onContentHeightMeasured = { height -> reportedBlockHeight = height },
        usesBigScanButton = { bigButton -> usesBigScanButton = bigButton },
        onMenu = onMenu,
        onScan = containerState.onScan,
    )

    if (containerState.showMenu) {
        BoxWithConstraints(
            modifier = Modifier
                .constrainAs(menuRef) {
                    bottom.linkTo(homeBarRef.top)
                    start.linkTo(homeBarRef.start)
                    if (usesBigScanButton) {
                        end.linkTo(homeBarRef.end, margin = Sizes.s08)
                    } else {
                        end.linkTo(parent.end, margin = Sizes.s04)
                    }
                    width = Dimension.fillToConstraints
                }
        ) {
            HomeCompactMenu(
                preferredWidth = maxWidth,
                showEIdRequestButton = containerState.showEIdRequestButton,
                showBetaIdRequestButton = containerState.showBetaIdRequestButton,
                onMenu = onMenu,
                onGetEId = containerState.onGetEId,
                onGetBetaId = containerState.onGetBetaId,
                onSettings = containerState.onSettings,
                onHelp = containerState.onHelp,
            )
        }
    }
}

@Composable
private fun HomeCompactStickyBottom(
    modifier: Modifier,
    onContentHeightMeasured: (stickyBottomHeight: Dp) -> Unit,
    usesBigScanButton: (Boolean) -> Unit,
    onMenu: (Boolean) -> Unit,
    onScan: () -> Unit,
) {
    HeightReportingLayout(
        modifier = modifier,
        onContentHeightMeasured = onContentHeightMeasured,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(bottom = Sizes.s06)
                .bottomSafeDrawing(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            HomeBarHorizontal(
                onScan = onScan,
                onMenu = { onMenu(true) },
                usesBigScanButton = usesBigScanButton,
                modifier = Modifier.setIsTraversalGroup(index = TraversalIndex.HIGH3)
            )
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun HomeLargeContainer(
    containerState: HomeContainerState,
    onMenu: (Boolean) -> Unit,
    content: @Composable BoxScope.(stickyBottomHeightDp: Dp) -> Unit,
) = ConstraintLayout(
    modifier = Modifier.fillMaxSize()
) {
    val (
        contentRef,
        homeBarRef,
        menuRef
    ) = createRefs()

    HomeBarVertical(
        modifier = Modifier
            .constrainAs(homeBarRef) {
                start.linkTo(parent.start)
                top.linkTo(parent.top, margin = Sizes.s10)
                bottom.linkTo(parent.bottom, margin = Sizes.s10)
            }
            .setIsTraversalGroup(index = TraversalIndex.HIGH3)
            .padding(horizontal = Sizes.s02)
            .verticalSafeDrawing()
            .startSafeDrawing(),
        onScan = containerState.onScan,
        onMenu = { onMenu(true) },
    )

    Box(
        modifier = Modifier
            .constrainAs(contentRef) {
                start.linkTo(homeBarRef.end)
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                width = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
            }
            .endSafeDrawing()
    ) {
        content(0.dp)
    }

    if (containerState.showMenu) {
        BoxWithConstraints(
            modifier = Modifier
                .constrainAs(menuRef) {
                    start.linkTo(homeBarRef.end)
                    top.linkTo(homeBarRef.top)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                }
        ) {
            HomeLargeMenu(
                preferredWidth = maxWidth * 0.4f,
                showEIdRequestButton = containerState.showEIdRequestButton,
                showBetaIdRequestButton = containerState.showBetaIdRequestButton,
                onMenu = onMenu,
                onGetEId = containerState.onGetEId,
                onGetBetaId = containerState.onGetBetaId,
                onSettings = containerState.onSettings,
                onHelp = containerState.onHelp,
            )
        }
    }
}
