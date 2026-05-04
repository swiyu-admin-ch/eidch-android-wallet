package ch.admin.foitt.wallet.platform.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.centerHorizontallyOnFullscreen
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun ErrorScreenContent(
    @DrawableRes iconRes: Int,
    title: String,
    body: String,
    primaryButton: String,
    onPrimaryClick: () -> Unit,
    secondaryButton: String? = null,
    onSecondaryClick: () -> Unit = {},
) {
    val modifier = Modifier
        .background(WalletTheme.colorScheme.surfaceContainerHigh)
        .centerHorizontallyOnFullscreen()
    when (currentWindowAdaptiveInfo().windowWidthClass()) {
        WindowWidthClass.COMPACT -> WalletLayouts.CompactContainerFloatingBottom(
            modifier = modifier,
            content = {
                MainContent(
                    iconRes = iconRes,
                    title = title,
                    body = body,
                )
            },
            stickyBottomContent = {
                StickyBottomContent(
                    primaryButton = primaryButton,
                    onPrimaryClick = onPrimaryClick,
                    secondaryButton = secondaryButton,
                    onSecondaryClick = onSecondaryClick,
                )
            },
        )
        else -> WalletLayouts.LargeContainerFloatingBottom(
            modifier = modifier,
            content = {
                MainContent(
                    iconRes = iconRes,
                    title = title,
                    body = body,
                )
            },
            stickyBottomContent = {
                StickyBottomContent(
                    primaryButton = primaryButton,
                    onPrimaryClick = onPrimaryClick,
                    secondaryButton = secondaryButton,
                    onSecondaryClick = onSecondaryClick,
                )
            },
        )
    }
}

@Composable
private fun MainContent(
    @DrawableRes iconRes: Int,
    title: String,
    body: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Sizes.s04, vertical = Sizes.s06),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(Sizes.s14),
            tint = WalletTheme.colorScheme.onSurface,
        )
        WalletTexts.TitleMedium(
            modifier = Modifier.semantics { heading() },
            text = title,
            textAlign = TextAlign.Center,
        )
        WalletTexts.BodyLarge(
            text = body,
            color = WalletTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StickyBottomContent(
    primaryButton: String,
    onPrimaryClick: () -> Unit,
    secondaryButton: String?,
    onSecondaryClick: () -> Unit,
) = FlowRow(
    horizontalArrangement = Arrangement.spacedBy(Sizes.s04, Alignment.CenterHorizontally),
    verticalArrangement = Arrangement.spacedBy(Sizes.s02),
    modifier = Modifier.fillMaxWidth(),
) {
    secondaryButton?.let {
        Buttons.FilledSecondary(
            text = secondaryButton,
            onClick = onSecondaryClick,
        )
    }
    Buttons.FilledPrimary(
        text = primaryButton,
        onClick = onPrimaryClick,
    )
}

private val longText by lazy {
    "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, " +
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna " +
        "aliquyam erat, sed diam voluptua. Lorem ipsum dolor sit amet, " +
        "consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt " +
        "ut labore et dolore magna aliquyam erat, sed diam voluptua. " +
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, " +
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, " +
        "sed diam voluptua. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, " +
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, " +
        "sed diam voluptua. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, " +
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, " +
        "sed diam voluptua. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, " +
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, " +
        "sed diam voluptua."
}

@Composable
@WalletAllScreenPreview
private fun ResultScreenPreview() {
    WalletTheme {
        ErrorScreenContent(
            iconRes = R.drawable.wallet_ic_shield_cross,
            title = "Title text",
            body = longText,
            primaryButton = "Primary button text",
            onPrimaryClick = {},
        )
    }
}
