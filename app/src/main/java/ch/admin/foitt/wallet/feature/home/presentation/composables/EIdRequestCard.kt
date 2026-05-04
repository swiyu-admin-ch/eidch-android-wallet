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

@Suppress("CyclomaticComplexMethod")
@Composable
fun EIdRequestCard(
    eIdRequest: SIdRequestDisplayData,
    onMainButton: () -> Unit,
    onClose: () -> Unit,
) = when (eIdRequest.status) {
    SIdRequestDisplayStatus.AV_READY,
    SIdRequestDisplayStatus.AV_READY_LEGAL_CONSENT_OK -> CardReady(eIdRequest, onMainButton)
    SIdRequestDisplayStatus.AV_READY_LEGAL_CONSENT_PENDING -> CardReadyConsentPending(eIdRequest, onMainButton)
    SIdRequestDisplayStatus.QUEUEING,
    SIdRequestDisplayStatus.QUEUEING_LEGAL_CONSENT_OK -> CardQueueing(eIdRequest)
    SIdRequestDisplayStatus.QUEUEING_LEGAL_CONSENT_PENDING -> CardQueueingConsentPending(onMainButton)
    SIdRequestDisplayStatus.AV_EXPIRED,
    SIdRequestDisplayStatus.AV_EXPIRED_LEGAL_CONSENT_OK,
    SIdRequestDisplayStatus.AV_EXPIRED_LEGAL_CONSENT_PENDING -> CardTimeout(eIdRequest, onClose)
    SIdRequestDisplayStatus.UNKNOWN -> CardUnknown(eIdRequest, onMainButton)
    SIdRequestDisplayStatus.IN_AGENT_REVIEW -> CardInAgentReview(eIdRequest)
    SIdRequestDisplayStatus.IN_ISSUANCE -> CardInIssuance(eIdRequest)
    SIdRequestDisplayStatus.REFUSED -> CardRefused(eIdRequest, onMainButton, onClose)
    SIdRequestDisplayStatus.IN_TARGET_WALLET_PAIRING -> CardInTargetWalletPairing(eIdRequest, onMainButton)
    SIdRequestDisplayStatus.IN_AUTO_VERIFICATION -> CardInAutoVerification(eIdRequest, onMainButton)
    SIdRequestDisplayStatus.READY_FOR_FINAL_ENTITLEMENT_CHECK -> CardFinalEntitlementCheck(eIdRequest)
    SIdRequestDisplayStatus.CANCELLED -> CardCancelled(eIdRequest, onClose)
    SIdRequestDisplayStatus.CLOSED -> CardClosed(eIdRequest, onClose)
}

@Composable
private fun CardReady(
    displayData: SIdRequestDisplayData,
    onStart: () -> Unit,
) = EIdRequestCardGeneric(
    title = stringResource(R.string.tk_getEid_notification_eidReady_primary, displayData.cardName),
    body = stringResource(
        R.string.tk_getEid_notification_eidReady_secondary,
        displayData.onlineSessionStartTimeoutAt ?: ""
    ),
    buttonText = stringResource(R.string.tk_getEid_notification_eidReady_button),
    onButtonClick = onStart,
)

@Composable
private fun CardReadyConsentPending(
    displayData: SIdRequestDisplayData,
    onObtainConsent: () -> Unit,
) = EIdRequestCardGeneric(
    title = stringResource(R.string.tk_getEid_notification_eidReady_noConsent_primary),
    body = stringResource(
        R.string.tk_getEid_notification_eidReady_noConsent_secondary,
        displayData.onlineSessionStartTimeoutAt ?: ""
    ),
    buttonText = stringResource(R.string.tk_getEid_notification_eidReady_noConsent_button),
    onButtonClick = onObtainConsent,
)

@Composable
private fun CardQueueing(
    displayData: SIdRequestDisplayData,
) = EIdRequestCardGeneric(
    title = stringResource(R.string.tk_getEid_notification_eidProgress_primary, displayData.cardName),
    body = stringResource(
        R.string.tk_getEid_notification_eidProgress_secondary,
        displayData.onlineSessionStartOpenAt ?: ""
    )
)

@Composable
private fun CardQueueingConsentPending(
    onObtainConsent: () -> Unit,
) = EIdRequestCardGeneric(
    title = stringResource(R.string.tk_getEid_notification_eidQueue_noConsent_primary),
    body = stringResource(R.string.tk_getEid_notification_eidQueue_noConsent_secondary),
    buttonText = stringResource(R.string.tk_getEid_notification_eidQueue_noConsent_button),
    onButtonClick = onObtainConsent,
)

@Composable
private fun CardUnknown(
    displayData: SIdRequestDisplayData,
    onRefresh: () -> Unit,
) = EIdRequestCardGeneric(
    title = stringResource(R.string.tk_getEid_notification_unknown_primary, displayData.cardName),
    body = stringResource(R.string.tk_getEid_notification_unknown_secondary),
    useTertiaryButton = false,
    buttonText = stringResource(R.string.tk_getEid_notification_unknown_button_refresh),
    onButtonClick = onRefresh,
)

@Composable
private fun CardInAgentReview(
    displayData: SIdRequestDisplayData,
) = EIdRequestCardGeneric(
    title = stringResource(R.string.tk_getEid_notification_agentReview_primary, displayData.cardName),
    body = stringResource(R.string.tk_getEid_notification_agentReview_secondary),
)

@Composable
private fun CardInIssuance(
    displayData: SIdRequestDisplayData,
) = EIdRequestCardGeneric(
    title = stringResource(R.string.tk_getEid_notification_issuing_primary, displayData.cardName),
    body = stringResource(R.string.tk_getEid_notification_issuing_secondary),
)

@Composable
private fun CardInTargetWalletPairing(
    displayData: SIdRequestDisplayData,
    onWalletPairing: () -> Unit,
) = EIdRequestCardGeneric(
    title = stringResource(R.string.tk_getEid_notification_walletPairing_primary, displayData.cardName),
    body = stringResource(R.string.tk_getEid_notification_walletPairing_secondary),
    buttonText = stringResource(R.string.tk_getEid_notification_walletPairing_button),
    onButtonClick = onWalletPairing,
)

@Composable
private fun CardInAutoVerification(
    displayData: SIdRequestDisplayData,
    onAutoVerification: () -> Unit,
) = EIdRequestCardGeneric(
    title = stringResource(R.string.tk_getEid_notification_autoVerification_primary, displayData.cardName),
    body = stringResource(R.string.tk_getEid_notification_autoVerification_secondary),
    buttonText = stringResource(R.string.tk_getEid_notification_autoVerification_button),
    onButtonClick = onAutoVerification,
)

@Composable
private fun CardFinalEntitlementCheck(
    displayData: SIdRequestDisplayData,
) = EIdRequestCardGeneric(
    title = stringResource(R.string.tk_getEid_notification_readyForFinalEntitlementCheck_primary, displayData.cardName),
    body = stringResource(R.string.tk_getEid_notification_readyForFinalEntitlementCheck_secondary),
)

@Composable
private fun CardRefused(
    displayData: SIdRequestDisplayData,
    onLearnMore: () -> Unit,
    onClose: () -> Unit,
) = EIdRequestCardGeneric(
    title = stringResource(R.string.tk_getEid_notification_declined_primary, displayData.cardName),
    body = stringResource(R.string.tk_getEid_notification_declined_secondary),
    buttonText = stringResource(R.string.tk_getEid_notification_declined_button),
    onButtonClick = onLearnMore,
    onCloseClick = onClose,
)

@Composable
private fun CardCancelled(
    displayData: SIdRequestDisplayData,
    onClose: () -> Unit,
) = EIdRequestCardGeneric(
    title = stringResource(R.string.tk_getEid_notification_cancelled_primary, displayData.cardName),
    body = stringResource(R.string.tk_getEid_notification_cancelled_secondary),
    onCloseClick = onClose,
)

@Composable
private fun CardClosed(
    displayData: SIdRequestDisplayData,
    onClose: () -> Unit,
) = EIdRequestCardGeneric(
    title = stringResource(R.string.tk_getEid_notification_closed_primary, displayData.cardName),
    body = stringResource(R.string.tk_getEid_notification_closed_secondary),
    onCloseClick = onClose,
)

@Composable
private fun CardTimeout(
    displayData: SIdRequestDisplayData,
    onClose: () -> Unit,
) = EIdRequestCardGeneric(
    title = stringResource(R.string.tk_getEid_notification_eidExpired_primary, displayData.cardName),
    body = stringResource(R.string.tk_getEid_notification_eidExpired_secondary),
    onCloseClick = onClose,
)

@Composable
private fun EIdRequestCardGeneric(
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

private val SIdRequestDisplayData.cardName get() = "$firstName $lastName"

//region Preview
private class EIdRequestCardPreviewParams : PreviewParameterProvider<SIdRequestDisplayData> {
    override val values: Sequence<SIdRequestDisplayData> = SIdRequestDisplayStatus.entries.map {
        it.toPreviewDisplayData()
    }.asSequence()
}

private fun SIdRequestDisplayStatus.toPreviewDisplayData() = SIdRequestDisplayData(
    status = this,
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
            onMainButton = {},
            onClose = {},
        )
    }
}
//endregion
