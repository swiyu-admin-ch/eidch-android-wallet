package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.InvitationHeader
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.composables.AdaptiveButtonContainer
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.HeightReportingLayout
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletButtonColors
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun CredentialActionFeedbackCardError(
    modifier: Modifier = Modifier,
    issuer: ActorUiState,
    contentTextFirstParagraphText: String? = null,
    contentTextSecondParagraphText: String? = null,
    iconAlwaysVisible: Boolean = false,
    @DrawableRes contentIcon: Int? = null,
    backgroundColor: Color = WalletTheme.colorScheme.surfaceContainerHighest,
    textColor: Color = WalletTheme.colorScheme.onSurface,
    secondaryTextColor: Color = WalletTheme.colorScheme.onSurfaceVariant,
    primaryButtonColors: ButtonColors = WalletButtonColors.feedbackFailurePrimary(),
    secondaryButtonColors: ButtonColors = WalletButtonColors.feedbackFailureSecondary(),
    primaryButtonText: String? = null,
    @StringRes secondaryButtonText: Int? = null,
    onPrimaryButton: (() -> Unit)? = null,
    onSecondaryButton: (() -> Unit)? = null,
    onActorNameTap: () -> Unit,
    onReportedActorInfo: () -> Unit
) {
    CredentialActionFeedbackCard(
        modifier = modifier,
        issuer = issuer,
        contentTextFirstParagraphText = contentTextFirstParagraphText,
        contentTextSecondParagraphText = contentTextSecondParagraphText,
        iconAlwaysVisible = iconAlwaysVisible,
        contentIcon = contentIcon,
        backgroundColor = backgroundColor,
        textColor = textColor,
        secondaryTextColor = secondaryTextColor,
        primaryButtonColors = primaryButtonColors,
        secondaryButtonColors = secondaryButtonColors,
        primaryButtonText = primaryButtonText,
        secondaryButtonText = secondaryButtonText,
        onPrimaryButton = onPrimaryButton,
        onSecondaryButton = onSecondaryButton,
        onActorNameTap = onActorNameTap,
        onReportedActorInfo = onReportedActorInfo
    )
}

@Composable
fun CredentialActionFeedbackCardSuccess(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    issuer: ActorUiState,
    contentTextFirstParagraphText: String? = null,
    contentTextSecondParagraphText: String? = null,
    contentTextThirdParagraphText: String? = null,
    iconAlwaysVisible: Boolean = false,
    @DrawableRes contentIcon: Int? = null,
    backgroundColor: Color = WalletTheme.colorScheme.tertiary,
    textColor: Color = WalletTheme.colorScheme.lightTertiary,
    primaryButtonColors: ButtonColors = WalletButtonColors.feedbackSuccessPrimary(),
    primaryButtonText: String? = null,
    onPrimaryButton: (() -> Unit)? = null,
    onSecondaryButton: (() -> Unit)? = null,
    onActorNameTap: () -> Unit,
    onReportedActorInfo: () -> Unit
) {
    CredentialActionFeedbackCard(
        modifier = modifier,
        isLoading = isLoading,
        issuer = issuer,
        contentTextFirstParagraphText = contentTextFirstParagraphText,
        contentTextSecondParagraphText = contentTextSecondParagraphText,
        contentTextThirdParagraphText = contentTextThirdParagraphText,
        iconAlwaysVisible = iconAlwaysVisible,
        contentIcon = contentIcon,
        backgroundColor = backgroundColor,
        textColor = textColor,
        primaryButtonColors = primaryButtonColors,
        primaryButtonText = primaryButtonText,
        onPrimaryButton = onPrimaryButton,
        onSecondaryButton = onSecondaryButton,
        onActorNameTap = onActorNameTap,
        onReportedActorInfo = onReportedActorInfo
    )
}

@Composable
fun CredentialActionFeedbackCard(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    issuer: ActorUiState,
    contentTextFirstParagraphText: String? = null,
    contentTextSecondParagraphText: String? = null,
    contentTextThirdParagraphText: String? = null,
    iconAlwaysVisible: Boolean = false,
    @DrawableRes contentIcon: Int? = null,
    backgroundColor: Color = WalletTheme.colorScheme.primary,
    textColor: Color = WalletTheme.colorScheme.lightPrimary,
    secondaryTextColor: Color = WalletTheme.colorScheme.lightPrimary,
    primaryButtonColors: ButtonColors = WalletButtonColors.feedbackDeclinePrimary(),
    secondaryButtonColors: ButtonColors = WalletButtonColors.feedbackDeclineSecondary(),
    ternaryButtonColors: ButtonColors = WalletButtonColors.feedbackDeclineSecondary(),
    primaryButtonText: String? = null,
    @StringRes secondaryButtonText: Int? = null,
    @StringRes ternaryButtonText: Int? = null,
    onPrimaryButton: (() -> Unit)? = null,
    onSecondaryButton: (() -> Unit)? = null,
    onTernaryButton: (() -> Unit)? = null,
    onActorNameTap: () -> Unit,
    onReportedActorInfo: () -> Unit
) {
    val headerHeight = remember { mutableStateOf(0.dp) }
    val stickyBottomHeight = remember { mutableStateOf(0.dp) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(WalletTheme.colorScheme.surfaceContainerLow)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Header(
                issuer = issuer,
                headerHeight = headerHeight,
                onActorNameTap = onActorNameTap,
                onReportedActorInfo = onReportedActorInfo
            )

            val minHeight = this@BoxWithConstraints.maxHeight - headerHeight.value
            Sheet(
                modifier = Modifier.heightIn(min = minHeight),
                stickyBottomHeight = stickyBottomHeight.value,
                iconAlwaysVisible = iconAlwaysVisible,
                contentTextFirstParagraph = contentTextFirstParagraphText,
                contentTextSecondParagraph = contentTextSecondParagraphText,
                contentTextThirdParagraph = contentTextThirdParagraphText,
                contentIcon = contentIcon,
                backgroundColor = backgroundColor,
                textColor = textColor,
                secondaryTextColor = secondaryTextColor,
            )
        }
        StickyBottomButtons(
            modifier = Modifier.align(Alignment.BottomCenter),
            stickyBottomHeight = stickyBottomHeight,
            onPrimaryButton = onPrimaryButton,
            onSecondaryButton = onSecondaryButton,
            onTernaryButton = onTernaryButton,
            primaryButtonText = primaryButtonText,
            secondaryButtonText = secondaryButtonText,
            primaryButtonColors = primaryButtonColors,
            secondaryButtonColors = secondaryButtonColors,
            ternaryButtonText = ternaryButtonText,
            ternaryButtonColors = ternaryButtonColors,
        )
        LoadingOverlay(showOverlay = isLoading)
    }
}

@Composable
private fun Header(
    headerHeight: MutableState<Dp>,
    issuer: ActorUiState,
    onActorNameTap: () -> Unit,
    onReportedActorInfo: () -> Unit
) = HeightReportingLayout(
    onContentHeightMeasured = { height -> headerHeight.value = height }
) {
    Column {
        InvitationHeader(
            modifier = Modifier.padding(horizontal = Sizes.s04),
            actorUiState = issuer,
            onActorNameTap = onActorNameTap,
            onReportedActorInfo = onReportedActorInfo
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
    }
}

@Composable
private fun Sheet(
    modifier: Modifier = Modifier,
    stickyBottomHeight: Dp,
    iconAlwaysVisible: Boolean,
    contentTextFirstParagraph: String?,
    contentTextSecondParagraph: String?,
    contentTextThirdParagraph: String?,
    @DrawableRes contentIcon: Int?,
    backgroundColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
) = Box(
    modifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(topStart = Sizes.boxCornerSize, topEnd = Sizes.boxCornerSize))
        .background(backgroundColor)
        .padding(top = Sizes.s06, start = Sizes.s06, end = Sizes.s06),
    contentAlignment = Alignment.Center
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = Sizes.s06 + stickyBottomHeight),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val compact = currentWindowAdaptiveInfoV2().windowWidthClass() == WindowWidthClass.COMPACT
        if ((iconAlwaysVisible || compact) && contentIcon != null) {
            Icon(
                modifier = Modifier
                    .height(Sizes.s14)
                    .width(Sizes.s14),
                painter = painterResource(id = contentIcon),
                contentDescription = null,
                tint = textColor,
            )
            Spacer(modifier = Modifier.height(Sizes.s01))
        }
        if (contentTextFirstParagraph != null) {
            WalletTexts.TitleMedium(
                text = contentTextFirstParagraph,
                color = textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .semantics {
                        if (contentTextSecondParagraph != null || contentTextThirdParagraph != null) {
                            heading()
                        }
                        liveRegion = LiveRegionMode.Assertive
                    }
                    .testTag(TestTags.DECLINE_SCREEN_TITLE.name),
            )
        }
        if (contentTextSecondParagraph != null) {
            Spacer(modifier = Modifier.height(Sizes.s02))
            WalletTexts.BodyLarge(
                text = contentTextSecondParagraph,
                color = secondaryTextColor,
                textAlign = TextAlign.Center,
            )
        }
        if (contentTextThirdParagraph != null) {
            Spacer(modifier = Modifier.height(Sizes.s02))
            WalletTexts.BodyLarge(
                text = contentTextThirdParagraph,
                color = secondaryTextColor,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StickyBottomButtons(
    modifier: Modifier,
    stickyBottomHeight: MutableState<Dp>,
    primaryButtonText: String?,
    @StringRes secondaryButtonText: Int?,
    @StringRes ternaryButtonText: Int?,
    primaryButtonColors: ButtonColors,
    secondaryButtonColors: ButtonColors,
    ternaryButtonColors: ButtonColors,
    onPrimaryButton: (() -> Unit)?,
    onSecondaryButton: (() -> Unit)?,
    onTernaryButton: (() -> Unit)?,
) = HeightReportingLayout(
    modifier = modifier,
    onContentHeightMeasured = { height -> stickyBottomHeight.value = height }
) {
    AdaptiveButtonContainer(
        buttons = buildList {
            if (onPrimaryButton != null && primaryButtonText != null) {
                add(
                    {
                        Buttons.Text(
                            text = primaryButtonText,
                            onClick = onPrimaryButton,
                            colors = primaryButtonColors,
                            modifier = Modifier.testTag(TestTags.ACCEPT_BUTTON.name)
                        )
                    }
                )
            }
            if (onTernaryButton != null && ternaryButtonText != null) {
                add(
                    {
                        Buttons.Text(
                            text = stringResource(id = ternaryButtonText),
                            onClick = onTernaryButton,
                            colors = ternaryButtonColors
                        )
                    }
                )
            }
            if (onSecondaryButton != null && secondaryButtonText != null) {
                add(
                    {
                        Buttons.Text(
                            text = stringResource(id = secondaryButtonText),
                            onClick = onSecondaryButton,
                            colors = secondaryButtonColors,
                            modifier = Modifier.testTag(TestTags.DECLINE_BUTTON.name)
                        )
                    }
                )
            }
        },
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            .padding(Sizes.s04)
            .focusGroup()
    )
}

@WalletAllScreenPreview
@Composable
private fun CredentialActionFeedbackCardPreview() {
    WalletTheme {
        CredentialActionFeedbackCard(
            issuer = ActorUiState(
                name = "Test Issuer",
                painter = painterResource(id = R.drawable.wallet_ic_scan_person),
                trustStatus = TrustStatus.TRUSTED,
                vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
                actorType = ActorType.ISSUER,
                actorComplianceState = ActorComplianceState.REPORTED,
                nonComplianceReason = "report reason",
            ),
            contentTextFirstParagraphText = stringResource(R.string.tk_receive_declineOffer_primary),
            contentTextSecondParagraphText = stringResource(R.string.tk_receive_declineOffer_secondary),
            contentTextThirdParagraphText = stringResource(R.string.tk_getBetaId_error_smallbody),
            contentIcon = R.drawable.wallet_ic_circular_questionmark,
            iconAlwaysVisible = true,
            onSecondaryButton = {},
            onTernaryButton = {},
            onPrimaryButton = {},
            primaryButtonText = stringResource(R.string.tk_receive_declineOffer_primaryButton),
            secondaryButtonText = R.string.tk_global_cancel,
            ternaryButtonText = R.string.tk_receive_declineOffer_ternaryButton,
            onActorNameTap = {},
            onReportedActorInfo = {}
        )
    }
}
