package ch.admin.foitt.wallet.platform.activityList.presentation.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.composables.presentation.clusterLazyListItem
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletListItems
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Suppress("LongParameterList")
fun LazyListScope.activityListItem(
    activityType: ActivityType,
    activityId: Long,
    activityActorName: String,
    activityDate: String,
    showActorName: Boolean = true,
    isFirstItem: Boolean,
    isLastItem: Boolean,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    onClick: ((Long) -> Unit)? = null,
) = clusterLazyListItem(
    isFirstItem = isFirstItem,
    isLastItem = isLastItem,
    paddingValues = paddingValues,
) {
    when (activityType) {
        ActivityType.ISSUANCE -> {
            CredentialAcceptedActivityListItem(
                id = activityId,
                issuer = if (showActorName) activityActorName else null,
                date = activityDate,
                onClick = onClick,
            )
        }

        ActivityType.PRESENTATION_ACCEPTED -> {
            PresentationAcceptedActivityListItem(
                id = activityId,
                verifier = if (showActorName) activityActorName else null,
                date = activityDate,
                onClick = onClick,
            )
        }

        ActivityType.PRESENTATION_DECLINED -> {
            PresentationDeclinedActivityListItem(
                id = activityId,
                verifier = if (showActorName) activityActorName else null,
                date = activityDate,
                onClick = onClick,
            )
        }
    }
}

@Composable
private fun CredentialAcceptedActivityListItem(
    id: Long,
    issuer: String?,
    date: String,
    onClick: ((Long) -> Unit)?,
) = ActivityListItem(
    id = id,
    title = stringResource(R.string.tk_activity_credentialAccepted_title),
    subtitle = issuer,
    supportingText = date,
    leadingContent = {
        Icon(
            painter = painterResource(R.drawable.wallet_ic_circled_plus),
            contentDescription = null,
            tint = WalletTheme.colorScheme.onLightTertiary,
        )
    },
    onClick = onClick,
)

@Composable
private fun PresentationAcceptedActivityListItem(
    id: Long,
    verifier: String?,
    date: String,
    onClick: ((Long) -> Unit)?,
) = ActivityListItem(
    id = id,
    title = stringResource(R.string.tk_activity_presentationAccepted_title),
    subtitle = verifier,
    supportingText = date,
    leadingContent = {
        Icon(
            painter = painterResource(R.drawable.wallet_ic_circled_checkmark),
            contentDescription = null,
            tint = WalletTheme.colorScheme.onLightTertiary,
        )
    },
    onClick = onClick,
)

@Composable
private fun PresentationDeclinedActivityListItem(
    id: Long,
    verifier: String?,
    date: String,
    onClick: ((Long) -> Unit)?,
) = ActivityListItem(
    id = id,
    title = stringResource(R.string.tk_activity_presentationDeclined_title),
    subtitle = verifier,
    supportingText = date,
    leadingContent = {
        Icon(
            painter = painterResource(R.drawable.wallet_ic_circled_cross),
            contentDescription = null,
            tint = WalletTheme.colorScheme.onLightError,
        )
    },
    onClick = onClick,
)

fun LazyListScope.emptyHistoryActivityListItem(
    paddingValues: PaddingValues = PaddingValues(0.dp),
) = clusterLazyListItem(
    isFirstItem = true,
    isLastItem = true,
    paddingValues = paddingValues,
) {
    EmptyHistoryActivityListItem()
}

@Composable
fun EmptyHistoryActivityListItem() = ActivityListItem(
    title = stringResource(R.string.tk_activity_latestActivities_noHistory_title),
    supportingText = stringResource(R.string.tk_activity_latestActivities_noHistory_body),
    leadingContent = {
        Icon(
            painter = painterResource(R.drawable.wallet_ic_no_history),
            contentDescription = null,
            tint = WalletTheme.colorScheme.onSurfaceVariant,
        )
    },
)

fun LazyListScope.entireHistoryButton(
    paddingValues: PaddingValues,
    onClick: () -> Unit
) = clusterLazyListItem(
    isFirstItem = false,
    isLastItem = true,
    paddingValues = paddingValues,
) {
    EntireHistoryButton(
        onClick = onClick,
    )
}

@Composable
private fun EntireHistoryButton(
    onClick: () -> Unit,
) = ListItem(
    modifier = Modifier
        .clickable(onClick = onClick)
        .spaceBarKeyClickable(onClick)
        .semantics {
            role = Role.Button
        },
    colors = ListItemDefaults.colors(containerColor = WalletTheme.colorScheme.listItemBackground),
    headlineContent = {
        WalletTexts.BodyLarge(
            text = stringResource(R.string.tk_activity_latestActivities_entireHistory),
            color = WalletTheme.colorScheme.onSurface,
        )
    },
    leadingContent = { Spacer(modifier = Modifier.width(Sizes.s06)) },
    trailingContent = {
        Icon(
            painter = painterResource(R.drawable.wallet_ic_chevron_right),
            contentDescription = null,
            tint = WalletTheme.colorScheme.onSurface,
        )
    }
)

fun LazyListScope.disabledHistoryActivityListItem(
    paddingValues: PaddingValues = PaddingValues(0.dp),
) = clusterLazyListItem(
    isFirstItem = true,
    isLastItem = false,
    paddingValues = paddingValues,
) {
    DisabledHistoryActivityListItem()
}

@Composable
fun DisabledHistoryActivityListItem() = ActivityListItem(
    title = stringResource(R.string.tk_activity_latestActivities_noHistory_title),
    supportingText = stringResource(R.string.tk_activity_latestActivities_disabledHistory_body),
    leadingContent = {
        Icon(
            painter = painterResource(R.drawable.wallet_ic_no_history),
            contentDescription = null,
            tint = WalletTheme.colorScheme.onSurfaceVariant,
        )
    },
)

fun LazyListScope.goToHistorySettingsButton(
    paddingValues: PaddingValues,
    onClick: () -> Unit
) = clusterLazyListItem(
    isFirstItem = false,
    isLastItem = true,
    paddingValues = paddingValues,
) {
    GoToHistorySettingsButton(
        onClick = onClick,
    )
}

@Composable
private fun GoToHistorySettingsButton(
    onClick: () -> Unit,
) = ListItem(
    modifier = Modifier
        .clickable(onClick = onClick)
        .spaceBarKeyClickable(onClick),
    colors = ListItemDefaults.colors(containerColor = WalletTheme.colorScheme.listItemBackground),
    headlineContent = {
        WalletTexts.BodyLarge(
            text = stringResource(R.string.tk_activity_latestActivities_goToSettings),
            color = WalletTheme.colorScheme.onSurface,
        )
    },
    leadingContent = { Spacer(modifier = Modifier.width(Sizes.s06)) },
    trailingContent = {
        Icon(
            painter = painterResource(R.drawable.wallet_ic_chevron_right),
            contentDescription = null,
            tint = WalletTheme.colorScheme.onSurface,
        )
    }
)

@Composable
private fun ActivityListItem(
    id: Long = -1,
    title: String,
    subtitle: String? = null,
    supportingText: String? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    onClick: ((Long) -> Unit)? = null,
) = ListItem(
    modifier = Modifier
        .then(
            if (onClick == null) {
                Modifier
            } else {
                Modifier
                    .clickable { onClick(id) }
                    .spaceBarKeyClickable { onClick(id) }
            }
        ),
    colors = ListItemDefaults.colors(containerColor = WalletTheme.colorScheme.listItemBackground),
    headlineContent = {
        Column {
            WalletTexts.BodyLarge(
                text = title,
                color = WalletTheme.colorScheme.onSurface,
            )
            subtitle?.let {
                WalletTexts.BodyLarge(
                    text = it,
                    color = WalletTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    },
    supportingContent = {
        supportingText?.let {
            WalletTexts.BodyMedium(
                text = it,
                color = WalletTheme.colorScheme.onSurfaceVariant,
            )
        }
    },
    leadingContent = leadingContent,
    trailingContent = {
        onClick?.let {
            Icon(
                painter = painterResource(R.drawable.wallet_ic_chevron_right),
                contentDescription = null,
                tint = WalletTheme.colorScheme.onSurface,
            )
        }
    }
)

@WalletComponentPreview
@Composable
private fun ActivityListItemPreview() {
    WalletTheme {
        Column {
            CredentialAcceptedActivityListItem(
                id = 1,
                issuer = "Preview issuer",
                date = "01.01.2025 | 12:34",
                onClick = {},
            )
            WalletListItems.Divider()
            PresentationAcceptedActivityListItem(
                id = 2,
                verifier = "Preview issuer",
                date = "01.01.2025 | 12:34",
                onClick = {}
            )
            WalletListItems.Divider()
            PresentationDeclinedActivityListItem(
                id = 3,
                verifier = "Preview issuer",
                date = "01.01.2025 | 12:34",
                onClick = {},
            )
            WalletListItems.Divider()
            EmptyHistoryActivityListItem()
            WalletListItems.Divider()
            EntireHistoryButton(
                onClick = {},
            )
            WalletListItems.Divider()
            DisabledHistoryActivityListItem()
            WalletListItems.Divider()
            GoToHistorySettingsButton(
                onClick = {},
            )
        }
    }
}
