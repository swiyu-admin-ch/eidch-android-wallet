package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.clusterFooter
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialMocks
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

fun LazyListScope.credentialInfoWithTrustBadgesWidget(
    credentialCardState: CredentialCardState,
    paddingValues: PaddingValues = PaddingValues(
        horizontal = Sizes.s04
    ),
    footerText: Int? = null,
) {
    credentialCardListItem(
        credentialCardState = credentialCardState,
        paddingValues = paddingValues,
    )
    footerText?.let {
        clusterFooter(
            paddingValues = paddingValues,
            text = footerText
        )
    }
}

@WalletAllScreenPreview
@Composable
private fun CredentialInfoWithTrustBadgesWidgetPreview() {
    val cardState = CredentialMocks.cardState01
    WalletTheme {
        WalletLayouts.LazyColumn {
            credentialInfoWithTrustBadgesWidget(
                credentialCardState = cardState,
                footerText = R.string.tk_activity_activityDetail_credential_footer,
            )
        }
    }
}
