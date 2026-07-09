package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialCardMocks
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialDisplayStatus
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.preview.ComposableWrapper
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletListItems
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

fun LazyListScope.credentialCardListItem(
    credentialCardState: CredentialCardState,
    paddingValues: PaddingValues = PaddingValues(),
    showStatus: Boolean = true,
    isFirstItem: Boolean = true,
    isLastItem: Boolean = true,
    divider: (@Composable () -> Unit)? = WalletListItems::CredentialDivider,
    onClick: (() -> Unit)? = null,
) = item {
    CredentialCardListItem(
        credentialCardState = credentialCardState,
        showStatus = showStatus,
        isFirstItem = isFirstItem,
        isLastItem = isLastItem,
        divider = divider,
        paddingValues = paddingValues,
        onClick = onClick,
    )
}

fun LazyListScope.credentialCardListItems(
    credentialCardStates: List<CredentialCardState>,
    showStatus: Boolean = true,
    paddingValues: PaddingValues = PaddingValues(),
    onClick: ((credentialCardState: CredentialCardState) -> Unit)?,
) = itemsIndexed(items = credentialCardStates) { index, credentialCardState ->
    CredentialCardListItem(
        credentialCardState = credentialCardState,
        showStatus = showStatus,
        isFirstItem = index == credentialCardStates.indices.first,
        isLastItem = index == credentialCardStates.lastIndex,
        paddingValues = paddingValues,
        onClick = onClick?.let { { it(credentialCardState) } },
    )
}

@Composable
fun CredentialCardListItem(
    credentialCardState: CredentialCardState,
    showStatus: Boolean,
    isFirstItem: Boolean,
    isLastItem: Boolean,
    divider: (@Composable () -> Unit)? = WalletListItems::CredentialDivider,
    paddingValues: PaddingValues = PaddingValues(),
    onClick: (() -> Unit)?,
) = WalletListItems.Cluster(
    isFirstItem = isFirstItem,
    isLastItem = isLastItem,
    divider = divider,
    paddingValues = paddingValues,
) {
    ListItem(
        modifier = Modifier
            .padding(vertical = Sizes.s01)
            .run {
                onClick?.let { onClick ->
                    this.clickable(onClick = onClick)
                        .spaceBarKeyClickable(onClick)
                } ?: this
            },
        colors = ListItemDefaults.colors(
            headlineColor = WalletTheme.colorScheme.onSurfaceVariant,
            trailingIconColor = WalletTheme.colorScheme.onSurfaceVariant,
            containerColor = WalletTheme.colorScheme.listItemBackground
        ),
        leadingContent = {
            CredentialCardVerySmall(credentialCardState)
        },
        trailingContent = onClick?.let {
            {
                Icon(
                    painter = painterResource(id = R.drawable.wallet_ic_chevron_right),
                    contentDescription = null,
                )
            }
        },
        headlineContent = {
            Column {
                credentialCardState.title?.let {
                    WalletTexts.BodyLarge(
                        text = it,
                        color = WalletTheme.colorScheme.onSurface,
                    )
                }
                credentialCardState.subtitle?.let {
                    WalletTexts.BodyLarge(
                        text = it,
                        color = WalletTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (showStatus) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StatusBadges(credentialCardState)
                    }
                }
            }
        },
    )
}

@Composable
private fun StatusBadges(credentialState: CredentialCardState) {
    when {
        credentialState.deferredStatus != null -> DeferredCredentialStatus(credentialState.deferredStatus)
        credentialState.progressionState == VerifiableProgressionState.UNACCEPTED -> UnacceptedCredentialStatus()
        credentialState.status != null -> {
            // Demo badge is only shown on accepted credentials
            if (credentialState.isCredentialFromBetaIssuer) {
                DemoBadge()
                Spacer(modifier = Modifier.width(Sizes.s02))
            }
            CredentialStatus(status = credentialState.status)
        }
    }
}

@Composable
private fun CredentialStatus(
    status: CredentialDisplayStatus,
) = CredentialListBadge(
    text = status.getText(),
    contentColor = status.getContentColor(),
    iconRes = status.getIcon(),
)

@Composable
private fun UnacceptedCredentialStatus() = ReadyBadge()

@Composable
private fun DeferredCredentialStatus(
    deferredState: DeferredProgressionState,
) = CredentialListBadge(
    text = deferredState.getText(),
    contentColor = WalletTheme.colorScheme.onSurfaceVariant,
    iconRes = deferredState.getIcon(),
)

@Composable
private fun CredentialListBadge(
    text: String,
    contentColor: Color,
    @DrawableRes iconRes: Int,
) {
    val bodyTextHeight = with(LocalDensity.current) {
        WalletTheme.typography.bodyMedium.lineHeight.toDp()
    }

    Icon(
        painter = painterResource(id = iconRes),
        contentDescription = null,
        tint = contentColor,
        modifier = Modifier.sizeIn(
            maxWidth = bodyTextHeight,
            maxHeight = bodyTextHeight,
        )
    )
    Spacer(modifier = Modifier.width(Sizes.s01))
    WalletTexts.Body(
        text = text,
        color = contentColor,
    )
}

@Composable
private fun CredentialDisplayStatus.getContentColor() = when (this) {
    CredentialDisplayStatus.Valid,
    CredentialDisplayStatus.Unsupported,
    CredentialDisplayStatus.Unknown -> WalletTheme.colorScheme.onSurfaceVariant
    is CredentialDisplayStatus.NotYetValid,
    is CredentialDisplayStatus.Expired,
    is CredentialDisplayStatus.BusinessExpired,
    CredentialDisplayStatus.Revoked,
    CredentialDisplayStatus.Suspended -> WalletTheme.colorScheme.error
}

//region Preview
private class CredentialCardListItemPreviewParams : PreviewParameterProvider<ComposableWrapper<CredentialCardState>> {
    override val values = CredentialCardMocks.mocks
}

@WalletComponentPreview
@Composable
private fun CredentialCardListItemPreview(
    @PreviewParameter(CredentialCardListItemPreviewParams::class) cardState: ComposableWrapper<CredentialCardState>,
) {
    WalletTheme {
        val cardState = cardState.value()
        LazyColumn {
            credentialCardListItem(
                credentialCardState = cardState,
                onClick = {},
                isFirstItem = true,
                isLastItem = false,
            )
            credentialCardListItem(
                credentialCardState = cardState,
                onClick = null,
                isFirstItem = false,
                isLastItem = false,
                showStatus = false,
            )
            credentialCardListItem(
                credentialCardState = cardState,
                onClick = {},
                isFirstItem = false,
                isLastItem = true,
            )
        }
    }
}
//endregion
