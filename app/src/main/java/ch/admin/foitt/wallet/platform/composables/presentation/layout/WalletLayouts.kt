package ch.admin.foitt.wallet.platform.composables.presentation.layout

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import ch.admin.foitt.wallet.platform.composables.presentation.HeightReportingLayout
import ch.admin.foitt.wallet.platform.composables.presentation.WindowHeightClass
import ch.admin.foitt.wallet.platform.composables.presentation.bottomSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.topSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.windowHeightClass
import ch.admin.foitt.wallet.platform.scaffold.presentation.LocalScaffoldPaddings
import ch.admin.foitt.wallet.theme.Sizes

object WalletLayouts {
    //region Layout constants
    private val paddingStickySmall = Sizes.s02
    val paddingStickyMedium = Sizes.s04
    val paddingContentBottom = Sizes.s06

    val stickyBottomPaddingValuesPortrait = PaddingValues(
        top = Sizes.s03,
        start = paddingStickyMedium,
        end = paddingStickyMedium,
        bottom = paddingStickyMedium,
    )

    private val stickyBottomPaddingValuesLandscape = PaddingValues(
        vertical = paddingStickySmall,
        horizontal = paddingStickyMedium,
    )

    private val stickStartPaddingValuesLandscape = PaddingValues(
        start = paddingStickySmall,
        bottom = paddingStickySmall
    )

    const val cardCompactScreenRatio = 0.33f
    const val cardLargeScreenRatio = 0.5f
    //endregion

    @Composable
    fun isHeightCompact() = currentWindowAdaptiveInfo().windowHeightClass() == WindowHeightClass.COMPACT

    @Composable
    private fun getCardScreenRatio() = if (isHeightCompact()) {
        cardCompactScreenRatio
    } else {
        cardLargeScreenRatio
    }

    private fun Modifier.handleContainerInsets(
        shouldScrollUnderTopBar: Boolean,
        scaffoldPaddings: PaddingValues,
    ): Modifier =
        fillMaxSize()
            .run {
                if (!shouldScrollUnderTopBar) {
                    padding(scaffoldPaddings)
                        .consumeWindowInsets(scaffoldPaddings)
                } else {
                    this
                }
            }
            .imePadding()

    @Composable
    fun TopInsetSpacer(
        shouldScrollUnderTopBar: Boolean,
        scaffoldPaddings: PaddingValues,
    ) {
        val modifier = Modifier
            .run {
                if (shouldScrollUnderTopBar) {
                    val topPadding = scaffoldPaddings.calculateTopPadding()
                    this
                        .padding(top = topPadding)
                        .consumeWindowInsets(WindowInsets(top = topPadding))
                } else {
                    this.topSafeDrawing()
                }
            }
        Spacer(modifier)
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun LargeContainer(
        modifier: Modifier = Modifier,
        cardScreenRatio: Float = getCardScreenRatio(),
        contentHeightDimension: Dimension = Dimension.fillToConstraints,
        contentScrollState: ScrollState,
        isStickyStartScrollable: Boolean = false,
        stickyStartPadding: PaddingValues = stickStartPaddingValuesLandscape,
        stickyStartContent: @Composable ColumnScope.() -> Unit,
        stickyBottomPadding: PaddingValues = stickyBottomPaddingValuesLandscape,
        stickyBottomHorizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(Sizes.s02, Alignment.End),
        stickyBottomBackgroundColor: Color = Color.Transparent,
        stickyBottomContent: @Composable (RowScope.() -> Unit)?,
        onBottomHeightMeasured: (Dp) -> Unit,
        contentPadding: PaddingValues = PaddingValues(),
        scaffoldPaddings: PaddingValues = LocalScaffoldPaddings.current,
        shouldScrollUnderTopBar: Boolean = true,
        content: @Composable ColumnScope.() -> Unit,
    ) = ConstraintLayout(
        modifier = modifier
            .handleContainerInsets(
                shouldScrollUnderTopBar,
                scaffoldPaddings,
            )
            .horizontalSafeDrawing()
    ) {
        var bottomBlockHeightDp by remember {
            mutableStateOf(0.dp)
        }
        val (
            stickyStartRef,
            mainContentRef,
            stickyBottomRef,
        ) = createRefs()

        Column(
            modifier = Modifier
                .then(
                    if (isStickyStartScrollable) {
                        Modifier
                            .verticalScroll(contentScrollState)
                            .scrollable(
                                contentScrollState,
                                orientation = Orientation.Vertical,
                                reverseDirection = true,
                            )
                    } else {
                        Modifier
                    }
                )
                .padding(stickyStartPadding)
                .constrainAs(stickyStartRef) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                    width = Dimension.percent(cardScreenRatio)
                }
        ) {
            TopInsetSpacer(
                shouldScrollUnderTopBar,
                scaffoldPaddings,
            )
            stickyStartContent()
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(mainContentRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(stickyStartRef.end)
                    end.linkTo(parent.end)
                    height = contentHeightDimension
                    width = Dimension.fillToConstraints
                }
                .verticalScroll(contentScrollState)
                .padding(contentPadding)
        ) {
            TopInsetSpacer(
                shouldScrollUnderTopBar,
                scaffoldPaddings,
            )
            content()
            Spacer(Modifier.height(bottomBlockHeightDp))
        }

        Box(
            modifier = Modifier
                .constrainAs(stickyBottomRef) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(stickyStartRef.end)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
        ) {
            HeightReportingLayout(
                onContentHeightMeasured = { height ->
                    bottomBlockHeightDp = height
                    onBottomHeightMeasured(height)
                },
            ) {
                if (stickyBottomContent == null) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(stickyBottomBackgroundColor)
                            .bottomSafeDrawing()
                    )
                } else {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(stickyBottomBackgroundColor)
                            .padding(stickyBottomPadding)
                            .bottomSafeDrawing()
                            .focusGroup(),
                        horizontalArrangement = stickyBottomHorizontalArrangement,
                        verticalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.Top),
                        maxItemsInEachRow = 2,
                    ) {
                        stickyBottomContent()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun CompactContainer(
        modifier: Modifier = Modifier,
        contentHeightDimension: Dimension = Dimension.fillToConstraints,
        stickyBottomPadding: PaddingValues = stickyBottomPaddingValuesPortrait,
        stickyBottomHorizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(Sizes.s02, Alignment.End),
        stickyBottomBackgroundColor: Color = Color.Transparent,
        stickyBottomContent: @Composable (RowScope.() -> Unit)?,
        scaffoldPaddings: PaddingValues = LocalScaffoldPaddings.current,
        shouldScrollUnderTopBar: Boolean = true,
        scrollState: ScrollState = rememberScrollState(),
        onBottomHeightMeasured: ((Dp) -> Unit)?,
        contentPadding: PaddingValues = PaddingValues(),
        content: @Composable ColumnScope.(BoxWithConstraintsScope) -> Unit,
    ) = ConstraintLayout(
        modifier = modifier
            .handleContainerInsets(
                shouldScrollUnderTopBar,
                scaffoldPaddings,
            )
    ) {
        var bottomBlockHeightDp by remember {
            mutableStateOf(0.dp)
        }
        val (
            mainContentRef,
            stickyBottomRef,
        ) = createRefs()

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(mainContentRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    height = contentHeightDimension
                }
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(contentPadding)
            ) {
                TopInsetSpacer(
                    shouldScrollUnderTopBar,
                    scaffoldPaddings,
                )
                content(this@BoxWithConstraints)
                stickyBottomContent?.let {
                    Spacer(Modifier.height(bottomBlockHeightDp))
                } ?: Spacer(Modifier.bottomSafeDrawing())
            }
        }

        if (stickyBottomContent != null) {
            HeightReportingLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(stickyBottomRef) {
                        bottom.linkTo(parent.bottom)
                    },
                onContentHeightMeasured = { height ->
                    bottomBlockHeightDp = height
                    onBottomHeightMeasured?.let { it(height) }
                },
            ) {
                FlowRow(
                    modifier = Modifier
                        .background(stickyBottomBackgroundColor)
                        .fillMaxWidth()
                        .padding(stickyBottomPadding)
                        .bottomSafeDrawing()
                        .focusGroup(),
                    horizontalArrangement = stickyBottomHorizontalArrangement,
                    verticalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.Top),
                    maxItemsInEachRow = 2,
                ) {
                    stickyBottomContent()
                }
            }
        }
    }

    /**
     * used for layouts with text input (e.g. passphrase screens)
     */
    @Composable
    fun CompactContainerFloatingBottom(
        modifier: Modifier = Modifier,
        verticalArrangement: Arrangement.Vertical = Arrangement.Center,
        auxiliaryContent: (@Composable () -> Unit)? = null,
        stickyBottomHorizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
        stickyBottomContent: @Composable ColumnScope.() -> Unit,
        scaffoldPaddings: PaddingValues = LocalScaffoldPaddings.current,
        shouldScrollUnderTopBar: Boolean = true,
        content: @Composable ColumnScope.() -> Unit,
    ) = ConstraintLayout(
        modifier = modifier.handleContainerInsets(
            shouldScrollUnderTopBar,
            scaffoldPaddings,
        )
    ) {
        var bottomBlockHeightDp by remember {
            mutableStateOf(0.dp)
        }

        val (
            contentRef,
            auxContentRef,
            stickyBottomRef,
        ) = createRefs()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(contentRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                }
                .padding(horizontal = Sizes.s04)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = verticalArrangement,
        ) {
            TopInsetSpacer(
                shouldScrollUnderTopBar,
                scaffoldPaddings,
            )
            content()
            Spacer(Modifier.height(bottomBlockHeightDp))
        }

        auxiliaryContent?.let {
            Column(
                modifier = Modifier
                    .constrainAs(auxContentRef) {
                        bottom.linkTo(stickyBottomRef.top)
                    }
            ) {
                auxiliaryContent()
            }
        }

        HeightReportingLayout(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(stickyBottomRef) {
                    bottom.linkTo(parent.bottom)
                },
            onContentHeightMeasured = { height -> bottomBlockHeightDp = height },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .bottomSafeDrawing()
                    .padding(Sizes.s04),
                horizontalAlignment = stickyBottomHorizontalAlignment,
            ) {
                stickyBottomContent()
            }
        }
    }

    /**
     * used for layouts with text input (e.g. passphrase screens)
     */
    @Composable
    fun LargeContainerFloatingBottom(
        modifier: Modifier = Modifier,
        verticalArrangement: Arrangement.Vertical = Arrangement.Center,
        auxiliaryContent: (@Composable () -> Unit)? = null,
        stickyBottomHorizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
        stickyBottomContent: (@Composable RowScope.() -> Unit)? = null,
        scaffoldPaddings: PaddingValues = LocalScaffoldPaddings.current,
        shouldScrollUnderTopBar: Boolean = true,
        content: @Composable ColumnScope.() -> Unit,
    ) = ConstraintLayout(
        modifier = modifier
            .handleContainerInsets(
                shouldScrollUnderTopBar,
                scaffoldPaddings,
            )
            .horizontalSafeDrawing()
    ) {
        var bottomBlockHeightDp by remember {
            mutableStateOf(0.dp)
        }

        val (
            contentRef,
            auxContentRef,
            stickyBottomRef,
        ) = createRefs()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(contentRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                }
                .padding(horizontal = Sizes.s04)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = verticalArrangement,
        ) {
            TopInsetSpacer(
                shouldScrollUnderTopBar,
                scaffoldPaddings,
            )
            content()
            stickyBottomContent?.let {
                Spacer(Modifier.height(bottomBlockHeightDp))
            } ?: Spacer(Modifier.bottomSafeDrawing())
        }

        stickyBottomContent?.let {
            HeightReportingLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(stickyBottomRef) {
                        bottom.linkTo(parent.bottom)
                    }
                    .background(Color.Transparent),
                onContentHeightMeasured = { height -> bottomBlockHeightDp = height },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Sizes.s04),
                    horizontalArrangement = stickyBottomHorizontalArrangement,
                ) {
                    stickyBottomContent()
                }
            }
        }

        auxiliaryContent?.let {
            Column(
                modifier = Modifier
                    .constrainAs(auxContentRef) {
                        bottom.linkTo(parent.bottom)
                    }
            ) {
                auxiliaryContent()
            }
        }
    }
}
