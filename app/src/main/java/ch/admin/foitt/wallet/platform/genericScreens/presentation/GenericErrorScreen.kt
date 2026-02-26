package ch.admin.foitt.wallet.platform.genericScreens.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.ErrorScreenContent
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun GenericErrorScreen(
    viewModel: GenericErrorViewModel,
) {
    ErrorScreenContent(
        onBack = viewModel::onBack,
    )
}

@Composable
private fun ErrorScreenContent(
    onBack: () -> Unit,
) {
    ErrorScreenContent(
        iconRes = R.drawable.wallet_ic_error_general,
        title = stringResource(id = R.string.presentation_error_title),
        body = stringResource(id = R.string.presentation_error_message),
        primaryButton = stringResource(id = R.string.global_error_backToHome_button),
        onPrimaryClick = onBack,
    )
}

@WalletAllScreenPreview
@Composable
fun ErrorScreenPreview() {
    WalletTheme {
        ErrorScreenContent(
            onBack = {},
        )
    }
}
