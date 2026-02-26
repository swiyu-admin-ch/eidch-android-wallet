@file:OptIn(ExperimentalMaterial3Api::class)

package ch.admin.foitt.wallet.platform.scaffold.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarBackground
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.platform.utils.TraversalIndex
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import ch.admin.foitt.wallet.theme.WalletTopBarColors

@Composable
fun WalletTopBar(
    viewModel: WalletTopBarViewModel = hiltViewModel(),
) {
    val currentState = viewModel.state.collectAsStateWithLifecycle().value
    WalletTopAppBarContent(
        topBarState = currentState,
    )
}

@Composable
private fun WalletTopAppBarContent(
    topBarState: TopBarState,
) {
    when (topBarState) {
        is TopBarState.DetailsWithCloseButton -> TopBarBackArrow(
            titleId = topBarState.titleId,
            colors = getBackgroundColor(topBarState.topBarBackground),
            onUp = topBarState.onUp,
            actionButton = {
                CloseButton(
                    onClose = topBarState.onClose
                )
            },
        )

        is TopBarState.Details -> TopBarBackArrow(
            titleId = topBarState.titleId,
            titleAltTextId = topBarState.titleAltTextId,
            colors = getBackgroundColor(topBarState.topBarBackground),
            onUp = topBarState.onUp,
            actionButton = {},
        )

        is TopBarState.EmptyWithCloseButton -> TopBarEmptyWithClose(
            actionButton = {
                CloseButton(
                    onClose = topBarState.onClose
                )
            },
        )

        is TopBarState.OnGradient -> TopAppBarOnGradient(
            titleId = topBarState.titleId,
            onUp = topBarState.onUp,
        )

        TopBarState.None -> {}
        TopBarState.Empty -> TopBarEmpty()
    }
}

@Composable
private fun TopBarEmptyWithClose(
    actionButton: @Composable () -> Unit,
) = TopAppBar(
    title = {},
    actions = {
        actionButton()
    },
    colors = WalletTopBarColors.transparent(),
)

@Composable
private fun TopBarEmpty() = TopAppBar(
    title = {},
    colors = WalletTopBarColors.transparent(),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TopBarBackArrow(
    modifier: Modifier = Modifier,
    @StringRes titleId: Int?,
    @StringRes titleAltTextId: Int? = null,
    showButtonBackground: Boolean = false,
    colors: TopAppBarColors = WalletTopBarColors.transparent(),
    onUp: () -> Unit,
    actionButton: @Composable () -> Unit,
) {
    TopAppBar(
        title = {
            titleId?.let {
                val altText = titleAltTextId?.let {
                    stringResource(it)
                }

                WalletTexts.TitleTopBar(
                    text = stringResource(id = titleId),
                    color = colors.titleContentColor,
                    modifier = Modifier
                        .semantics {
                            heading()
                            traversalIndex = TraversalIndex.HIGH2.value
                            if (altText != null) {
                                contentDescription = altText
                            }
                        }
                )
            }
        },
        navigationIcon = {
            BackButton(
                showButtonBackground = showButtonBackground,
                onUp = onUp,
                iconTint = colors.navigationIconContentColor,
                modifier = Modifier
                    .semantics {
                        traversalIndex = TraversalIndex.HIGH1.value
                    }
                    .testTag(TestTags.BACK_BUTTON.name)
            )
        },
        actions = {
            actionButton()
        },
        colors = colors,
        modifier = modifier,
    )
}

@Composable
private fun TopAppBarOnGradient(
    @StringRes titleId: Int,
    onUp: () -> Unit,
) = TopAppBar(
    title = {
        WalletTexts.TitleTopBar(
            modifier = Modifier
                .testTag(TestTags.TOP_BAR_TITLE.name),
            text = stringResource(id = titleId),
            color = WalletTheme.colorScheme.onGradientFixed,
        )
    },
    navigationIcon = {
        BackButton(
            iconTint = WalletTheme.colorScheme.onGradientFixed,
            showButtonBackground = false,
            onUp = onUp,
        )
    },
    colors = WalletTopBarColors.transparent(),
)

@Composable
internal fun TopBarTitleOnly(
    @StringRes titleId: Int,
    colors: TopAppBarColors = WalletTopBarColors.transparent(),
) = TopAppBar(
    title = {
        WalletTexts.TitleTopBar(
            text = stringResource(id = titleId),
            color = colors.titleContentColor,
            modifier = Modifier.semantics {
                heading()
                traversalIndex = -1f
            }
        )
    },
    colors = colors,
)

@Composable
private fun BackButton(
    modifier: Modifier = Modifier,
    iconTint: Color = WalletTheme.colorScheme.onSecondaryContainer,
    showButtonBackground: Boolean = false,
    onUp: () -> Unit,
) = TopBarButton(
    onClick = onUp,
    icon = R.drawable.wallet_ic_back_navigation,
    iconTint = iconTint,
    contentDescription = stringResource(id = R.string.tk_global_back_alt),
    modifier = modifier,
    buttonBackground = if (showButtonBackground) {
        WalletTheme.colorScheme.outline.copy(alpha = 0.24f)
    } else {
        null
    }
)

@Composable
private fun CloseButton(
    onClose: () -> Unit,
) = TopBarButton(
    onClick = onClose,
    icon = R.drawable.wallet_ic_cross,
    contentDescription = stringResource(id = R.string.tk_global_close_alt),
)

@Composable
fun TopBarButton(
    onClick: () -> Unit,
    @DrawableRes icon: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    iconTint: Color = WalletTheme.colorScheme.onSecondaryContainer,
    buttonBackground: Color? = null,
) {
    val backgroundModifier = if (buttonBackground != null) {
        Modifier.background(
            color = buttonBackground,
            shape = CircleShape,
        )
    } else {
        Modifier
    }

    IconButton(
        modifier = backgroundModifier
            .then(modifier)
            .spaceBarKeyClickable(onClick)
            .testTag(TestTags.BACK_BUTTON.name),
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = contentDescription,
            tint = iconTint,
        )
    }
}

@Composable
private fun getBackgroundColor(topBarBackground: TopBarBackground) = when (topBarBackground) {
    TopBarBackground.DEFAULT -> WalletTopBarColors.default()
    TopBarBackground.TRANSPARENT -> WalletTopBarColors.transparent()
    TopBarBackground.CLUSTER -> WalletTopBarColors.clusterScreen()
}

private class TopBarPreviewParamsProvider : PreviewParameterProvider<TopBarState> {
    override val values = sequenceOf(
        TopBarState.DetailsWithCloseButton(onUp = {}, titleId = R.string.tk_present_result_success_primary, onClose = {}),
        TopBarState.Details(onUp = {}, titleId = R.string.tk_settings_imprint_title),
        TopBarState.Details(onUp = {}, titleId = null),
        TopBarState.None
    )
}

@WalletComponentPreview
@Composable
private fun WalletTopBarPreview(
    @PreviewParameter(TopBarPreviewParamsProvider::class) previewParam: TopBarState,
) {
    WalletTheme {
        WalletTopAppBarContent(
            topBarState = previewParam,
        )
    }
}
