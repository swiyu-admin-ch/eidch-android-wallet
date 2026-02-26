package ch.admin.foitt.wallet.feature.home.presentation.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialCardVerySmallSquare
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SIdRequestDisplayData
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SIdRequestDisplayStatus
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun EIdRequestCard(
    eIdRequest: SIdRequestDisplayData,
    onStartOnlineIdentification: () -> Unit,
    onRefresh: () -> Unit,
    onObtainConsent: () -> Unit,
    onLearnMore: () -> Unit,
    onCloseClick: (() -> Unit)?,
) = when (eIdRequest.status) {
    SIdRequestDisplayStatus.AV_READY,
    SIdRequestDisplayStatus.AV_READY_LEGAL_CONSENT_OK -> EIdRequestCardGeneric(
        title = stringResource(
            R.string.tk_getEid_notification_eidReady_primary,
            "${eIdRequest.firstName} ${eIdRequest.lastName}"
        ),
        body = stringResource(
            R.string.tk_getEid_notification_eidReady_secondary,
            eIdRequest.onlineSessionStartTimeoutAt ?: "" // this can not be null here
        ),
        buttonText = stringResource(R.string.tk_getEid_notification_eidReady_greenButton),
        onButtonClick = onStartOnlineIdentification,
    )
    SIdRequestDisplayStatus.AV_READY_LEGAL_CONSENT_PENDING -> EIdRequestCardGeneric(
        title = stringResource(R.string.tk_getEid_notification_eidReady_noConsent_primary),
        body = stringResource(R.string.tk_getEid_notification_eidReady_noConsent_secondary),
        buttonText = stringResource(R.string.tk_getEid_notification_eidReady_noConsent_button),
        onButtonClick = onObtainConsent,
    )
    SIdRequestDisplayStatus.QUEUEING,
    SIdRequestDisplayStatus.QUEUEING_LEGAL_CONSENT_OK -> EIdRequestCardGeneric(
        title = stringResource(
            R.string.tk_getEid_notification_eidProgress_primary,
            "${eIdRequest.firstName} ${eIdRequest.lastName}"
        ),
        body = stringResource(
            R.string.tk_getEid_notification_eidProgress_secondary,
            eIdRequest.onlineSessionStartOpenAt ?: "" // this can not be null here
        )
    )
    SIdRequestDisplayStatus.QUEUEING_LEGAL_CONSENT_PENDING -> EIdRequestCardGeneric(
        title = stringResource(R.string.tk_getEid_notification_eidQueue_noConsent_primary),
        body = stringResource(R.string.tk_getEid_notification_eidQueue_noConsent_secondary),
        buttonText = stringResource(R.string.tk_getEid_notification_eidQueue_noConsent_button),
        onButtonClick = onObtainConsent,
    )
    SIdRequestDisplayStatus.AV_EXPIRED,
    SIdRequestDisplayStatus.AV_EXPIRED_LEGAL_CONSENT_OK,
    SIdRequestDisplayStatus.AV_EXPIRED_LEGAL_CONSENT_PENDING -> EIdRequestCardGeneric(
        title = stringResource(
            R.string.tk_getEid_notification_eidExpired_primary,
            "${eIdRequest.firstName} ${eIdRequest.lastName}"
        ),
        body = stringResource(
            R.string.tk_getEid_notification_eidExpired_secondary,
            eIdRequest.onlineSessionStartOpenAt ?: "" // this can not be null here
        ),
        onCloseClick = onCloseClick,
    )
    SIdRequestDisplayStatus.UNKNOWN -> EIdRequestCardGeneric(
        title = stringResource(
            R.string.tk_getEid_notification_unknown_primary,
            "${eIdRequest.firstName} ${eIdRequest.lastName}"
        ),
        body = stringResource(
            R.string.tk_getEid_notification_unknown_secondary,
        ),
        useTertiaryButton = false,
        buttonText = stringResource(R.string.tk_getEid_notification_unknown_button_refresh),
        onButtonClick = onRefresh,
    )
    SIdRequestDisplayStatus.IN_AGENT_REVIEW -> EIdRequestCardGeneric(
        title = stringResource(
            R.string.tk_getEid_notification_agentReview_primary,
            "${eIdRequest.firstName} ${eIdRequest.lastName}"
        ),
        body = stringResource(
            R.string.tk_getEid_notification_agentReview_secondary
        )
    )
    SIdRequestDisplayStatus.IN_ISSUANCE -> EIdRequestCardGeneric(
        title = stringResource(
            R.string.tk_getEid_notification_issuing_primary,
            "${eIdRequest.firstName} ${eIdRequest.lastName}"
        ),
        body = stringResource(R.string.tk_getEid_notification_issuing_secondary),
    )
    SIdRequestDisplayStatus.REFUSED -> EIdRequestCardGeneric(
        title = stringResource(
            R.string.tk_getEid_notification_declined_primary,
            "${eIdRequest.firstName} ${eIdRequest.lastName}"
        ),
        body = stringResource(
            R.string.tk_getEid_notification_declined_secondary
        ),
        buttonText = stringResource(R.string.tk_getEid_notification_declined_primaryButton),
        onButtonClick = onLearnMore,
    )
    SIdRequestDisplayStatus.OTHER -> { /* Nothing to show */ }
}

@Composable
fun EIdRequestCardGeneric(
    modifier: Modifier = Modifier,
    title: String,
    body: String,
    buttonText: String? = null,
    useTertiaryButton: Boolean = true,
    onButtonClick: () -> Unit = {},
    onCloseClick: (() -> Unit)? = null,
) = Surface(
    color = WalletTheme.colorScheme.surfaceContainerHighest,
    shape = RoundedCornerShape(Sizes.s05),
) {
    Row(
        modifier = modifier.padding(Sizes.s06),
    ) {
        CredentialCardVerySmallSquare()
        Spacer(modifier = Modifier.width(Sizes.s04))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            WalletTexts.TitleSmall(
                modifier = Modifier.semantics {
                    traversalIndex = -4f
                },
                text = title
            )
            WalletTexts.LabelLarge(
                modifier = Modifier.semantics {
                    traversalIndex = -3f
                },
                text = body
            )
            buttonText?.let {
                Spacer(modifier = Modifier.height(Sizes.s04))
                if (useTertiaryButton) {
                    Buttons.FilledTertiary(
                        modifier = Modifier.semantics {
                            traversalIndex = -2f
                        },
                        text = buttonText,
                        onClick = onButtonClick,
                    )
                } else {
                    Buttons.FilledPrimary(
                        modifier = Modifier.semantics {
                            traversalIndex = -2f
                        },
                        text = buttonText,
                        onClick = onButtonClick,
                    )
                }
            }
        }
        onCloseClick?.let {
            Spacer(modifier = Modifier.width(Sizes.s04))
            IconButton(
                modifier = Modifier
                    .size(Sizes.s08)
                    .padding(Sizes.s01)
                    .spaceBarKeyClickable(onCloseClick),
                onClick = onCloseClick,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.wallet_ic_cross),
                    contentDescription = "close",
                    tint = WalletTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}

private class EIdRequestCardPreviewParams : PreviewParameterProvider<SIdRequestDisplayData> {
    override val values: Sequence<SIdRequestDisplayData> = sequenceOf(
        getEIdRequestForPreview(SIdRequestDisplayStatus.AV_READY),
        getEIdRequestForPreview(SIdRequestDisplayStatus.AV_READY_LEGAL_CONSENT_OK),
        getEIdRequestForPreview(SIdRequestDisplayStatus.AV_READY_LEGAL_CONSENT_PENDING),
        getEIdRequestForPreview(SIdRequestDisplayStatus.QUEUEING),
        getEIdRequestForPreview(SIdRequestDisplayStatus.QUEUEING_LEGAL_CONSENT_OK),
        getEIdRequestForPreview(SIdRequestDisplayStatus.QUEUEING_LEGAL_CONSENT_PENDING),
        getEIdRequestForPreview(SIdRequestDisplayStatus.AV_EXPIRED),
        getEIdRequestForPreview(SIdRequestDisplayStatus.AV_EXPIRED_LEGAL_CONSENT_OK),
        getEIdRequestForPreview(SIdRequestDisplayStatus.AV_EXPIRED_LEGAL_CONSENT_PENDING),
        getEIdRequestForPreview(SIdRequestDisplayStatus.IN_AGENT_REVIEW),
        getEIdRequestForPreview(SIdRequestDisplayStatus.IN_ISSUANCE),
        getEIdRequestForPreview(SIdRequestDisplayStatus.UNKNOWN),
        getEIdRequestForPreview(SIdRequestDisplayStatus.OTHER),
    )
}

private fun getEIdRequestForPreview(queueingState: SIdRequestDisplayStatus) = SIdRequestDisplayData(
    status = queueingState,
    firstName = "Seraina",
    lastName = "Muster",
    onlineSessionStartOpenAt = "06.02.2025",
    onlineSessionStartTimeoutAt = "08.02.2025",
    caseId = "1",
)

@WalletComponentPreview
@Composable
private fun EIdRequestCardPreview(
    @PreviewParameter(EIdRequestCardPreviewParams::class) eIdRequest: SIdRequestDisplayData,
) {
    WalletTheme {
        EIdRequestCard(
            eIdRequest = eIdRequest,
            onStartOnlineIdentification = {},
            onRefresh = {},
            onObtainConsent = {},
            onCloseClick = {},
            onLearnMore = {}
        )
    }
}
