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
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.InvitationHeader
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.badges.domain.model.BadgeType
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.HeightReportingLayout
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
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
    @StringRes contentTextFirstParagraphText: Int? = null,
    @StringRes contentTextSecondParagraphText: Int? = null,
    iconAlwaysVisible: Boolean = false,
    @DrawableRes contentIcon: Int? = null,
    backgroundColor: Color = WalletTheme.colorScheme.surfaceContainerHighest,
    textColor: Color = WalletTheme.colorScheme.onSurface,
    secondaryTextColor: Color = WalletTheme.colorScheme.onSurfaceVariant,
    primaryButtonColors: ButtonColors = WalletButtonColors.feedbackFailurePrimary(),
    secondaryButtonColors: ButtonColors = WalletButtonColors.feedbackFailureSecondary(),
    content: @Composable (() -> Unit)? = null,
    @StringRes primaryButtonText: Int? = null,
    @StringRes secondaryButtonText: Int? = null,
    onPrimaryButton: (() -> Unit)? = null,
    onSecondaryButton: (() -> Unit)? = null,
    onBadge: (BadgeType) -> Unit,
) {
    CredentialActionFeedbackCard(
        modifier = modifier,
        issuer = issuer,
        contentTextFirstParagraphText = contentTextFirstParagraphText,
        contentTextSecondParagraphText = contentTextSecondParagraphText,
        content = content,
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
        onBadge = onBadge,
    )
}

@Composable
fun CredentialActionFeedbackCardSuccess(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    issuer: ActorUiState,
    @StringRes contentTextFirstParagraphText: Int? = null,
    @StringRes contentTextSecondParagraphText: Int? = null,
    @StringRes contentTextThirdParagraphText: Int? = null,
    iconAlwaysVisible: Boolean = false,
    @DrawableRes contentIcon: Int? = null,
    backgroundColor: Color = WalletTheme.colorScheme.tertiary,
    textColor: Color = WalletTheme.colorScheme.lightTertiary,
    primaryButtonColors: ButtonColors = WalletButtonColors.feedbackSuccessPrimary(),
    @StringRes primaryButtonText: Int? = null,
    content: (@Composable () -> Unit)?,
    onPrimaryButton: (() -> Unit)? = null,
    onSecondaryButton: (() -> Unit)? = null,
    onBadge: (BadgeType) -> Unit,
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
        content = content,
        onPrimaryButton = onPrimaryButton,
        onSecondaryButton = onSecondaryButton,
        onBadge = onBadge,
    )
}

@Composable
fun CredentialActionFeedbackCard(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    issuer: ActorUiState,
    @StringRes contentTextFirstParagraphText: Int? = null,
    @StringRes contentTextSecondParagraphText: Int? = null,
    @StringRes contentTextThirdParagraphText: Int? = null,
    iconAlwaysVisible: Boolean = false,
    @DrawableRes contentIcon: Int? = null,
    backgroundColor: Color = WalletTheme.colorScheme.primary,
    textColor: Color = WalletTheme.colorScheme.lightPrimary,
    secondaryTextColor: Color = WalletTheme.colorScheme.lightPrimary,
    primaryButtonColors: ButtonColors = WalletButtonColors.feedbackDeclinePrimary(),
    secondaryButtonColors: ButtonColors = WalletButtonColors.feedbackDeclineSecondary(),
    @StringRes primaryButtonText: Int? = null,
    @StringRes secondaryButtonText: Int? = null,
    content: (@Composable () -> Unit)? = null,
    onPrimaryButton: (() -> Unit)? = null,
    onSecondaryButton: (() -> Unit)? = null,
    onBadge: (BadgeType) -> Unit,
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
                onBadge = onBadge,
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
                content = content,
            )
        }
        StickyBottomButtons(
            modifier = Modifier.align(Alignment.BottomCenter),
            stickyBottomHeight = stickyBottomHeight,
            onPrimaryButton = onPrimaryButton,
            onSecondaryButton = onSecondaryButton,
            primaryButtonText = primaryButtonText,
            secondaryButtonText = secondaryButtonText,
            primaryButtonColors = primaryButtonColors,
            secondaryButtonColors = secondaryButtonColors,
        )
        LoadingOverlay(showOverlay = isLoading)
    }
}

@Composable
private fun Header(
    issuer: ActorUiState,
    headerHeight: MutableState<Dp>,
    onBadge: (BadgeType) -> Unit,
) = HeightReportingLayout(
    onContentHeightMeasured = { height -> headerHeight.value = height }
) {
    Column {
        InvitationHeader(
            modifier = Modifier.padding(horizontal = Sizes.s04),
            actorUiState = issuer,
            onBadge = onBadge,
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
    }
}

@Composable
private fun Sheet(
    modifier: Modifier = Modifier,
    stickyBottomHeight: Dp,
    iconAlwaysVisible: Boolean,
    @StringRes contentTextFirstParagraph: Int?,
    @StringRes contentTextSecondParagraph: Int?,
    @StringRes contentTextThirdParagraph: Int?,
    @DrawableRes contentIcon: Int?,
    backgroundColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    content: (@Composable () -> Unit)?,
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
    ) {
        val compact = currentWindowAdaptiveInfo().windowWidthClass() == WindowWidthClass.COMPACT
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
                text = stringResource(id = contentTextFirstParagraph),
                color = textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag(TestTags.DECLINE_SCREEN_TITLE.name)
            )
        }
        if (contentTextSecondParagraph != null) {
            Spacer(modifier = Modifier.height(Sizes.s01))
            WalletTexts.BodyLarge(
                text = stringResource(id = contentTextSecondParagraph),
                color = secondaryTextColor,
                textAlign = TextAlign.Center,
            )
        }
        if (contentTextThirdParagraph != null) {
            WalletTexts.BodySmall(
                text = stringResource(id = contentTextThirdParagraph),
                color = secondaryTextColor,
            )
        }
        if (content != null) {
            Spacer(modifier = Modifier.height(Sizes.s04))
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = if (!compact) 0.7f else 1f),
            ) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StickyBottomButtons(
    modifier: Modifier,
    stickyBottomHeight: MutableState<Dp>,
    @StringRes primaryButtonText: Int?,
    @StringRes secondaryButtonText: Int?,
    primaryButtonColors: ButtonColors,
    secondaryButtonColors: ButtonColors,
    onPrimaryButton: (() -> Unit)?,
    onSecondaryButton: (() -> Unit)?,
) = HeightReportingLayout(
    modifier = modifier,
    onContentHeightMeasured = { height -> stickyBottomHeight.value = height }
) {
    FlowRow(
        modifier = Modifier
            .padding(bottom = Sizes.s02)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            .focusGroup(),
        horizontalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.Top),
        maxItemsInEachRow = 2,
    ) {
        if (onPrimaryButton != null && primaryButtonText != null) {
            Buttons.Text(
                text = stringResource(id = primaryButtonText),
                onClick = onPrimaryButton,
                colors = primaryButtonColors,
                modifier = Modifier.testTag(TestTags.ACCEPT_BUTTON.name)
            )
        }
        if (onSecondaryButton != null && secondaryButtonText != null) {
            Buttons.Text(
                text = stringResource(id = secondaryButtonText),
                onClick = onSecondaryButton,
                colors = secondaryButtonColors,
                modifier = Modifier.testTag(TestTags.DECLINE_BUTTON.name)
            )
        }
    }
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
            contentTextFirstParagraphText = R.string.tk_receive_declineOffer_primary,
            contentTextSecondParagraphText = R.string.tk_receive_declineOffer_secondary,
            contentTextThirdParagraphText = R.string.tk_getBetaId_error_smallbody,
            contentIcon = R.drawable.wallet_ic_circular_questionmark,
            iconAlwaysVisible = true,
            onSecondaryButton = {},
            onPrimaryButton = {},
            primaryButtonText = R.string.tk_receive_declineOffer_primaryButton,
            secondaryButtonText = R.string.tk_global_cancel,
            onBadge = {},
        )
    }
}
