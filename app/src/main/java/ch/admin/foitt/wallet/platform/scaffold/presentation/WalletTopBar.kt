@file:OptIn(ExperimentalMaterial3Api::class)

package ch.admin.foitt.wallet.platform.scaffold.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
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
import ch.admin.foitt.wallet.platform.composables.presentation.actOnKeyWhenFocused
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarAction
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarBackground
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.platform.utils.TraversalIndex
import ch.admin.foitt.wallet.platform.utils.UiString
import ch.admin.foitt.wallet.theme.Sizes
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

        is TopBarState.DetailsWithCloseRoundButtons -> TopBarRoundButtons(
            titleId = topBarState.titleId,
            colors = getBackgroundColor(topBarState.topBarBackground),
            onUp = topBarState.onUp,
            actionButton = {
                CloseRoundButton(
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
            onAxDown = topBarState.onAXDown
        )

        is TopBarState.WithCloseButton -> TopBarWithClose(
            actionButton = {
                CloseButton(
                    onClose = topBarState.onClose,
                )
            },
            titleId = topBarState.titleId,
            titleAltTextId = topBarState.titleAltTextId,
            colors = getBackgroundColor(topBarState.topBarBackground),
        )

        is TopBarState.OnGradient -> TopAppBarOnGradient(
            titleId = topBarState.titleId,
            onUp = topBarState.onUp,
            onAxDown = topBarState.onAXDown
        )

        is TopBarState.Custom -> CustomTopBar(
            title = topBarState.title?.unwrap(),
            titleAltText = topBarState.titleAltText?.unwrap(),
            titleMaxLines = topBarState.titleMaxLines,
            onUp = topBarState.onUp,
            actions = topBarState.actions,
            colors = getBackgroundColor(topBarState.topBarBackground),
            useFilledButtons = topBarState.useFilledButtons,
            onAxDown = topBarState.onAXDown
        )

        TopBarState.None -> {}
        TopBarState.Empty -> TopBarEmpty()
    }
}

@Composable
private fun TopBarWithClose(
    actionButton: @Composable () -> Unit,
    @StringRes titleId: Int? = null,
    @StringRes titleAltTextId: Int? = null,
    colors: TopAppBarColors = WalletTopBarColors.transparent(),
) = TopAppBar(
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
                    .padding(
                        horizontal = Sizes.s04,
                        vertical = Sizes.s02,
                    )
            )
        }
    },
    actions = {
        actionButton()
    },
    colors = colors,
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
    backgroundGradient: Brush? = null,
    colors: TopAppBarColors = WalletTopBarColors.transparent(),
    onUp: () -> Unit,
    actionButton: @Composable () -> Unit,
    onAxDown: (() -> Unit)? = null
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
                        .actOnKeyWhenFocused(key = Key.DirectionDown, action = onAxDown)
                )
            }
        },
        navigationIcon = {
            // Providing the Back Button wrapped in a key allows the Accessibility (TalkBack) to recognize
            // the button as a new object even though the button and top bar don't actually change
            // we use onUp as key reference because that one only changes upon navigation events
            key(onUp) {
                BackButton(
                    showButtonBackground = showButtonBackground,
                    onUp = onUp,
                    iconTint = colors.navigationIconContentColor,
                    modifier = Modifier
                        .semantics {
                            traversalIndex = TraversalIndex.HIGH1.value
                        }
                        .testTag(TestTags.BACK_BUTTON.name)
                        .actOnKeyWhenFocused(key = Key.DirectionDown, action = onAxDown)
                )
            }
        },
        actions = {
            actionButton()
        },
        colors = colors,
        modifier = modifier
            .then(if (backgroundGradient != null) Modifier.background(backgroundGradient) else Modifier),
    )
}

@Composable
private fun TopAppBarOnGradient(
    @StringRes titleId: Int,
    onUp: () -> Unit,
    onAxDown: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            WalletTexts.TitleTopBar(
                modifier = Modifier
                    .testTag(TestTags.TOP_BAR_TITLE.name)
                    .actOnKeyWhenFocused(key = Key.DirectionDown, action = onAxDown)
                    .focusable(),
                text = stringResource(id = titleId),
                color = WalletTheme.colorScheme.onGradientFixed,
            )
        },
        navigationIcon = {
            BackButton(
                modifier = Modifier
                    .actOnKeyWhenFocused(key = Key.DirectionDown, action = onAxDown),
                iconTint = WalletTheme.colorScheme.onGradientFixed,
                showButtonBackground = false,
                onUp = onUp
            )
        },
        colors = WalletTopBarColors.transparent(),
    )
}

@Composable
internal fun TopBarTitleOnly(
    @StringRes titleId: Int,
    colors: TopAppBarColors = WalletTopBarColors.transparent(),
    actionButton: @Composable () -> Unit,
) = CenterAlignedTopAppBar(
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
    actions = {
        actionButton()
    },
    colors = colors,
)

@Composable
internal fun CustomTopBar(
    title: String?,
    titleAltText: String?,
    modifier: Modifier = Modifier,
    titleMaxLines: Int = Int.MAX_VALUE,
    onUp: (() -> Unit)? = null,
    actions: List<TopBarAction> = emptyList(),
    colors: TopAppBarColors = WalletTopBarColors.transparent(),
    useFilledButtons: Boolean = false,
    onAxDown: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            title?.let {
                WalletTexts.TitleTopBar(
                    text = title,
                    color = colors.titleContentColor,
                    modifier = Modifier
                        .semantics {
                            heading()
                            traversalIndex = TraversalIndex.HIGH2.value
                            if (titleAltText != null) {
                                contentDescription = titleAltText
                            }
                        },
                    maxLines = titleMaxLines,
                )
            }
        },
        modifier = modifier,
        navigationIcon = {
            if (onUp != null) {
                // Providing the Back Button wrapped in a key allows the Accessibility (TalkBack) to recognize
                // the button as a new object even though the button and top bar don't actually change
                // we use onUp as key reference because that one only changes upon navigation events
                key(onUp) {
                    CustomTopBarButton(
                        icon = R.drawable.wallet_ic_back_navigation,
                        onClick = onUp,
                        modifier = Modifier
                            .semantics {
                                traversalIndex = TraversalIndex.HIGH1.value
                            }
                            .testTag(TestTags.BACK_BUTTON.name)
                            .actOnKeyWhenFocused(key = Key.DirectionDown, action = onAxDown),
                        contentDescription = stringResource(R.string.tk_global_back_alt),
                        useFilledButtons = useFilledButtons,
                    )
                }
            }
        },
        actions = {
            for (action in actions) {
                CustomTopBarButton(
                    modifier = Modifier.actOnKeyWhenFocused(key = Key.DirectionDown, action = onAxDown),
                    icon = action.icon,
                    onClick = action.onClick,
                    contentDescription = action.contentDescription?.let { stringResource(it) },
                    useFilledButtons = useFilledButtons,
                )
            }
        },
        colors = colors,
    )
}

@Composable
fun CustomTopBarButton(
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    useFilledButtons: Boolean = false,
) {
    val colors = if (useFilledButtons) {
        IconButtonColors(
            containerColor = WalletTheme.colorScheme.onSecondaryFixed,
            contentColor = WalletTheme.colorScheme.secondaryFixed,
            disabledContainerColor = WalletTheme.colorScheme.onLightPrimary,
            disabledContentColor = WalletTheme.colorScheme.onLightPrimary
        )
    } else {
        IconButtonColors(
            containerColor = Color.Transparent,
            contentColor = WalletTheme.colorScheme.onSecondaryContainer,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = WalletTheme.colorScheme.onLightPrimary
        )
    }

    IconButton(
        onClick = onClick,
        modifier = modifier
            .spaceBarKeyClickable(onClick),
        colors = colors,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
        )
    }
}

@Composable
private fun BackButton(
    modifier: Modifier = Modifier,
    iconTint: Color = WalletTheme.colorScheme.onSecondaryContainer,
    showButtonBackground: Boolean = false,
    onUp: () -> Unit,
) = TopBarRoundButton(
    onClick = onUp,
    icon = R.drawable.wallet_ic_back_navigation,
    iconTint = iconTint,
    contentDescription = stringResource(id = R.string.tk_global_back_alt),
    modifier = modifier,
    backgroundColors = if (showButtonBackground) {
        IconButtonColors(
            containerColor = WalletTheme.colorScheme.outline.copy(alpha = 0.24f),
            contentColor = WalletTheme.colorScheme.outline.copy(alpha = 0.24f),
            disabledContainerColor = WalletTheme.colorScheme.outline.copy(alpha = 0.24f),
            disabledContentColor = WalletTheme.colorScheme.outline.copy(alpha = 0.24f)
        )
    } else {
        IconButtonColors(
            containerColor = Color.Transparent,
            contentColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.Transparent
        )
    }
)

@Composable
private fun CloseButton(
    onClose: () -> Unit,
) = TopBarRoundButton(
    onClick = onClose,
    icon = R.drawable.wallet_ic_cross,
    iconTint = WalletTheme.colorScheme.onSecondaryContainer,
    contentDescription = stringResource(id = R.string.tk_global_close_alt),
    backgroundColors = IconButtonColors(
        containerColor = Color.Transparent,
        contentColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        disabledContentColor = Color.Transparent
    )
)

/**
 * Used for EId scanner
 **/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TopBarRoundButtons(
    modifier: Modifier = Modifier,
    @StringRes titleId: Int?,
    @StringRes titleAltTextId: Int? = null,
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
                    color = WalletTheme.colorScheme.secondaryFixed,
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
            BackRoundButton(
                onUp = onUp,
                iconTint = WalletTheme.colorScheme.secondaryFixed,
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
fun TopBarRoundButton(
    onClick: () -> Unit,
    @DrawableRes icon: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    iconTint: Color = WalletTheme.colorScheme.secondaryFixed,
    backgroundColors: IconButtonColors = IconButtonColors(
        containerColor = WalletTheme.colorScheme.onSecondaryFixed,
        contentColor = WalletTheme.colorScheme.onSecondaryFixed,
        disabledContainerColor = WalletTheme.colorScheme.onLightPrimary,
        disabledContentColor = WalletTheme.colorScheme.onLightPrimary
    )
) {
    FilledIconButton(
        modifier = Modifier
            .padding(Sizes.s01)
            .then(modifier)
            .spaceBarKeyClickable(onClick)
            .testTag(TestTags.BACK_BUTTON.name),
        onClick = onClick,
        colors = backgroundColors
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = contentDescription,
            tint = iconTint,
        )
    }
}

@Composable
private fun BackRoundButton(
    modifier: Modifier = Modifier,
    iconTint: Color = WalletTheme.colorScheme.onSecondaryContainer,
    onUp: () -> Unit,
) = TopBarRoundButton(
    onClick = onUp,
    icon = R.drawable.wallet_ic_back_navigation,
    iconTint = iconTint,
    contentDescription = stringResource(id = R.string.tk_global_back_alt),
    modifier = modifier
)

@Composable
private fun CloseRoundButton(
    onClose: () -> Unit,
) = TopBarRoundButton(
    onClick = onClose,
    icon = R.drawable.wallet_ic_cross,
    contentDescription = stringResource(id = R.string.tk_global_close_alt),
)

@Composable
private fun getBackgroundColor(topBarBackground: TopBarBackground) = when (topBarBackground) {
    TopBarBackground.DEFAULT -> WalletTopBarColors.default()
    TopBarBackground.TRANSPARENT -> WalletTopBarColors.transparent()
    TopBarBackground.CLUSTER -> WalletTopBarColors.clusterScreen()
}

private class TopBarPreviewParamsProvider : PreviewParameterProvider<TopBarState> {
    override val values = sequenceOf(
        TopBarState.DetailsWithCloseButton(
            onUp = {},
            titleId = R.string.tk_present_result_data_transmitted_title,
            onClose = {},
        ),
        TopBarState.DetailsWithCloseRoundButtons(
            onUp = {},
            titleId = R.string.tk_eidRequest_recordDocument_verso,
            onClose = {},
        ),
        TopBarState.Details(onUp = {}, titleId = R.string.tk_settings_imprint_title),
        TopBarState.Details(onUp = {}, titleId = null),
        TopBarState.WithCloseButton(onClose = {}, titleId = R.string.tk_settings_imprint_title),
        TopBarState.None,
        TopBarState.Custom(
            title = UiString.Dynamic("Title"),
            onUp = {},
            actions = listOf(
                TopBarAction.Close(onClose = {})
            ),
            useFilledButtons = true,
            titleMaxLines = 1,
        )
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
