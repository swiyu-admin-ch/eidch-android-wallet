package ch.admin.foitt.wallet.platform.activityList.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.activityList.presentation.composables.ActivityDeleteConfirmationBottomSheet
import ch.admin.foitt.wallet.platform.activityList.presentation.composables.activityListItem
import ch.admin.foitt.wallet.platform.activityList.presentation.model.ActivityDetailScreenUiState
import ch.admin.foitt.wallet.platform.activityList.presentation.model.ActivityDetailUiState
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.badges.domain.model.BadgeType
import ch.admin.foitt.wallet.platform.badges.presentation.BadgeBottomSheet
import ch.admin.foitt.wallet.platform.badges.presentation.LegitimateActorBadge
import ch.admin.foitt.wallet.platform.badges.presentation.NonComplianceBadge
import ch.admin.foitt.wallet.platform.badges.presentation.TrustBadge
import ch.admin.foitt.wallet.platform.composables.Avatar
import ch.admin.foitt.wallet.platform.composables.AvatarSize
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.ToastAnimated
import ch.admin.foitt.wallet.platform.composables.presentation.clusterFooter
import ch.admin.foitt.wallet.platform.composables.presentation.clusterLazyListItem
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.credential.presentation.credentialClaimItems
import ch.admin.foitt.wallet.platform.credential.presentation.credentialInfoWithTrustBadgesWidget
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialDisplayStatus
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.scaffold.presentation.LocalScaffoldPaddings
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimCluster
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimText
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailScreen(viewModel: ActivityDetailViewModel) {
    val uiMode = LocalConfiguration.current.uiMode
    LaunchedEffect(uiMode) {
        viewModel.activityDetailUiState.refreshData()
    }

    val badgeBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val badgeBottomSheet = viewModel.badgeBottomSheet.collectAsStateWithLifecycle().value
    if (badgeBottomSheet != null) {
        BadgeBottomSheet(
            sheetState = badgeBottomSheetState,
            badgeBottomSheetUiState = badgeBottomSheet,
            onDismiss = viewModel::onDismissBadgeBottomSheet
        )
    }

    val confirmationSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val showConfirmationBottomSheet =
        viewModel.showConfirmationBottomSheet.collectAsStateWithLifecycle().value
    if (showConfirmationBottomSheet) {
        ActivityDeleteConfirmationBottomSheet(
            sheetState = confirmationSheetState,
            onCancel = viewModel::onDismissConfirmationBottomSheet,
            onDelete = viewModel::onDeleteActivityConfirmed
        )
    }

    ActivityDetailScreenContent(
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        nonComplianceEnabled = viewModel.nonComplianceEnabled,
        activityDetailScreenUiState = viewModel.activityDetailUiState.stateFlow.collectAsStateWithLifecycle().value,
        isSnackbarVisible = viewModel.isSnackbarVisible.collectAsStateWithLifecycle().value,
        onBadge = viewModel::onBadge,
        onReportActor = viewModel::onReportActor,
        onDeleteActivity = viewModel::onDeleteActivity,
        onCloseSnackbar = viewModel::hideNonComplianceSnackbar,
    )
}

@Composable
fun ActivityDetailScreenContent(
    isLoading: Boolean,
    nonComplianceEnabled: Boolean,
    activityDetailScreenUiState: ActivityDetailScreenUiState,
    isSnackbarVisible: Boolean,
    onBadge: (BadgeType.ActorInfoBadge) -> Unit,
    onReportActor: () -> Unit,
    onDeleteActivity: () -> Unit,
    onCloseSnackbar: () -> Unit,
) = Box(
    modifier = Modifier
        .fillMaxSize()
        .background(color = WalletTheme.colorScheme.surfaceContainerLow)
) {
    WalletLayouts.LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalSafeDrawing(),
        state = rememberLazyListState(),
        contentPadding = PaddingValues(top = Sizes.s02, bottom = Sizes.s04),
        useTopInsets = false,
    ) {
        val paddingValues = PaddingValues(horizontal = Sizes.s04)

        item {
            WalletLayouts.TopInsetSpacer(
                shouldScrollUnderTopBar = true,
                scaffoldPaddings = LocalScaffoldPaddings.current,
            )
        }

        activityListItem(
            activityType = activityDetailScreenUiState.activity.activityType,
            activityId = activityDetailScreenUiState.activity.id,
            activityActorName = activityDetailScreenUiState.activity.localizedActorName,
            activityDate = activityDetailScreenUiState.activity.date,
            showActorName = false,
            isFirstItem = true,
            isLastItem = true,
            paddingValues = paddingValues,
        )

        item { Spacer(modifier = Modifier.height(Sizes.s04)) }

        actorCluster(
            activityUiState = activityDetailScreenUiState.activity,
            paddingValues = paddingValues,
            actorTrustStatus = activityDetailScreenUiState.activity.actorTrust,
            actorActorComplianceState = activityDetailScreenUiState.activity.actorCompliance,
            activityType = activityDetailScreenUiState.activity.activityType,
            vcSchemaTrustStatus = activityDetailScreenUiState.activity.vcSchemaTrust,
            onBadge = onBadge,
        )

        item { Spacer(modifier = Modifier.height(Sizes.s04)) }

        when (activityDetailScreenUiState.activity.activityType) {
            ActivityType.PRESENTATION_ACCEPTED,
            ActivityType.PRESENTATION_DECLINED -> {
                item {
                    WalletTexts.TitleLargeEmphasized(
                        modifier = Modifier.padding(start = Sizes.s08, end = Sizes.s08, top = Sizes.s04),
                        text = "Shared data",
                    )
                }

                item { Spacer(modifier = Modifier.height(Sizes.s04)) }
            }
            else -> {}
        }

        item {
            WalletTexts.ClusterHeadline(
                text = stringResource(R.string.tk_activity_activityDetail_credential_title),
                depth = 0
            )
        }

        credentialInfoWithTrustBadgesWidget(
            credentialCardState = activityDetailScreenUiState.credential,
            paddingValues = paddingValues,
            footerText = R.string.tk_activity_activityDetail_credential_footer,
        )

        if (activityDetailScreenUiState.claims.isNotEmpty()) {
            item { Spacer(modifier = Modifier.height(Sizes.s04)) }

            credentialClaimItems(
                claimItems = activityDetailScreenUiState.claims,
            )
        }

        item { Spacer(modifier = Modifier.height(Sizes.s16)) }

        buttons(
            nonComplianceEnabled = nonComplianceEnabled,
            activityType = activityDetailScreenUiState.activity.activityType,
            paddingValues = paddingValues,
            onReportActor = onReportActor,
            onDeleteActivity = onDeleteActivity,
        )
    }

    ToastAnimated(
        isVisible = isSnackbarVisible,
        isSnackBarDesign = true,
        messageToast = R.string.tk_activity_activityList_nonCompliance_reportSent_title,
        iconEnd = R.drawable.wallet_ic_cross,
        onCloseToast = onCloseSnackbar,
    )

    LoadingOverlay(isLoading)
}

private fun LazyListScope.actorCluster(
    activityUiState: ActivityDetailUiState,
    paddingValues: PaddingValues,
    actorTrustStatus: TrustStatus,
    actorActorComplianceState: ActorComplianceState,
    activityType: ActivityType,
    vcSchemaTrustStatus: VcSchemaTrustStatus,
    onBadge: (BadgeType.ActorInfoBadge) -> Unit = {},
) {
    item {
        val title = when (activityUiState.activityType) {
            ActivityType.ISSUANCE -> R.string.tk_activity_activityDetail_issuer_title
            ActivityType.PRESENTATION_ACCEPTED,
            ActivityType.PRESENTATION_DECLINED -> R.string.tk_activity_activityDetail_verifier_title
        }

        WalletTexts.ClusterHeadline(
            text = stringResource(title),
            depth = 0
        )
    }

    clusterLazyListItem(
        isFirstItem = true,
        isLastItem = actorTrustStatus == TrustStatus.UNKNOWN && actorActorComplianceState == ActorComplianceState.UNKNOWN &&
            vcSchemaTrustStatus == VcSchemaTrustStatus.UNPROTECTED,
        showDivider = false,
        paddingValues = paddingValues,
    ) {
        val issuerIcon =
            activityUiState.actorImage ?: painterResource(id = R.drawable.wallet_ic_actor_default)
        ListItem(
            colors = ListItemDefaults.colors(containerColor = WalletTheme.colorScheme.listItemBackground),
            headlineContent = { WalletTexts.TitleMedium(text = activityUiState.localizedActorName) },
            leadingContent = {
                Avatar(
                    imagePainter = issuerIcon,
                    size = AvatarSize.SMALL,
                    imageTint = WalletTheme.colorScheme.onSurface,
                )
            },
        )
    }

    val title = when (activityUiState.activityType) {
        ActivityType.ISSUANCE -> R.string.tk_activity_activityDetail_trust_info_footer_issuer
        ActivityType.PRESENTATION_ACCEPTED,
        ActivityType.PRESENTATION_DECLINED -> R.string.tk_activity_activityDetail_trust_info_footer_verifier
    }

    actorTrustBadgeListItem(
        actorTrustStatus = actorTrustStatus,
        actorActorComplianceState = actorActorComplianceState,
        activityType = activityType,
        vcSchemaTrustStatus = vcSchemaTrustStatus,
        paddingValues = paddingValues,
        onBadge = onBadge,
    )

    clusterFooter(
        paddingValues = paddingValues,
        text = title,
    )
}

@OptIn(ExperimentalLayoutApi::class)
private fun LazyListScope.actorTrustBadgeListItem(
    actorTrustStatus: TrustStatus,
    actorActorComplianceState: ActorComplianceState,
    activityType: ActivityType,
    vcSchemaTrustStatus: VcSchemaTrustStatus,
    onBadge: (BadgeType.ActorInfoBadge) -> Unit,
    paddingValues: PaddingValues = PaddingValues(0.dp),
) = clusterLazyListItem(
    isFirstItem = false,
    isLastItem = true,
    showDivider = false,
    paddingValues = paddingValues,
) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = WalletTheme.colorScheme.listItemBackground),
        headlineContent = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Sizes.s03),
            ) {
                TrustBadge(
                    trustStatus = actorTrustStatus,
                    onClick = onBadge,
                )

                NonComplianceBadge(
                    actorComplianceState = actorActorComplianceState,
                    onClick = onBadge,
                )

                val actorType = when (activityType) {
                    ActivityType.ISSUANCE -> ActorType.ISSUER

                    ActivityType.PRESENTATION_ACCEPTED,
                    ActivityType.PRESENTATION_DECLINED -> ActorType.VERIFIER
                }

                LegitimateActorBadge(
                    actorType = actorType,
                    vcSchemaTrustStatus = vcSchemaTrustStatus,
                    onClick = onBadge,
                )
            }
        }
    )
}

private fun LazyListScope.buttons(
    nonComplianceEnabled: Boolean,
    activityType: ActivityType,
    paddingValues: PaddingValues,
    onReportActor: () -> Unit,
    onDeleteActivity: () -> Unit,
) {
    if (nonComplianceEnabled) {
        clusterLazyListItem(
            isFirstItem = true,
            isLastItem = false,
            paddingValues = paddingValues,
        ) {
            val title = when (activityType) {
                ActivityType.ISSUANCE -> R.string.tk_activity_activityDetail_reportIssuer_button
                ActivityType.PRESENTATION_ACCEPTED,
                ActivityType.PRESENTATION_DECLINED -> R.string.tk_activity_activityDetail_reportVerifier_button
            }
            Button(
                title = title,
                leadingIcon = R.drawable.wallet_ic_flag,
                onClick = onReportActor,
            )
        }
    }
    clusterLazyListItem(
        isFirstItem = !nonComplianceEnabled,
        isLastItem = true,
        paddingValues = paddingValues,
    ) {
        Button(
            title = R.string.tk_activity_activityDetail_deleteEntry_button,
            leadingIcon = R.drawable.wallet_ic_trashcan_big,
            onClick = onDeleteActivity,
        )
    }
}

@Composable
private fun Button(
    @StringRes title: Int,
    @DrawableRes leadingIcon: Int,
    onClick: () -> Unit,
) = ListItem(
    modifier = Modifier
        .clickable(onClick = onClick)
        .semantics {
            role = Role.Button
        }
        .spaceBarKeyClickable(onSpace = onClick),
    colors = ListItemDefaults.colors(containerColor = WalletTheme.colorScheme.listItemBackground),
    headlineContent = {
        WalletTexts.BodyLarge(
            text = stringResource(title),
            color = WalletTheme.colorScheme.onLightError,
        )
    },
    leadingContent = {
        Icon(
            painter = painterResource(leadingIcon),
            contentDescription = null,
            tint = WalletTheme.colorScheme.onLightError,
        )
    }
)

@WalletAllScreenPreview
@Composable
private fun ActivityDetailScreenPreview() {
    WalletTheme {
        ActivityDetailScreenContent(
            isLoading = false,
            nonComplianceEnabled = true,
            activityDetailScreenUiState = ActivityDetailScreenUiState(
                activity = ActivityDetailUiState(
                    id = 1,
                    activityType = ActivityType.ISSUANCE,
                    date = "01.01.2025 12:34",
                    localizedActorName = "Preview Issuer",
                    actorImage = painterResource(R.drawable.wallet_ic_actor_default),
                    actorTrust = TrustStatus.TRUSTED,
                    vcSchemaTrust = VcSchemaTrustStatus.TRUSTED,
                    actorCompliance = ActorComplianceState.REPORTED,
                ),
                credential = CredentialCardState(
                    credentialId = 1L,
                    title = "Elektronische Identität",
                    subtitle = "Seraina Muster",
                    status = CredentialDisplayStatus.Valid,
                    logo = painterResource(R.drawable.wallet_ic_swiss_cross),
                    backgroundColor = Color.Red,
                    contentColor = Color.White,
                    borderColor = Color.Red,
                    isCredentialFromBetaIssuer = false
                ),
                claims = listOf(
                    CredentialClaimCluster(
                        id = 1,
                        order = 1,
                        localizedLabel = "top level cluster",
                        parentId = null,
                        items = mutableListOf(
                            CredentialClaimText(
                                id = 1,
                                localizedLabel = "claim 1",
                                order = 1,
                                isSensitive = true,
                                value = "claim 1 value"
                            ),
                            CredentialClaimCluster(
                                id = 2,
                                order = 1,
                                localizedLabel = "cluster 2",
                                parentId = 1,
                                items = mutableListOf(
                                    CredentialClaimText(
                                        id = 2,
                                        localizedLabel = "claim 2",
                                        order = 1,
                                        isSensitive = false,
                                        value = "claim 2 value"
                                    ),
                                    CredentialClaimText(
                                        id = 3,
                                        localizedLabel = "claim 3",
                                        order = 2,
                                        isSensitive = true,
                                        value = "claim 3 value"
                                    ),
                                )
                            )
                        )
                    )
                )
            ),
            isSnackbarVisible = true,
            onBadge = {},
            onReportActor = {},
            onDeleteActivity = {},
            onCloseSnackbar = {},
        )
    }
}
