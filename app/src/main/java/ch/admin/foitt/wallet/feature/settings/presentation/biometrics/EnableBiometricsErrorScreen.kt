package ch.admin.foitt.wallet.feature.settings.presentation.biometrics

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.ErrorScreenContent
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun EnableBiometricsErrorScreen(viewModel: EnableBiometricsErrorViewModel) {
    EnableBiometricsErrorContent(
        errorCode = viewModel.errorCode,
        onClose = viewModel::onClose,
    )
}

@Composable
private fun EnableBiometricsErrorContent(
    errorCode: String,
    onClose: () -> Unit,
) = ErrorScreenContent(
    iconRes = R.drawable.wallet_ic_error_general,
    title = stringResource(R.string.tk_global_error_unexpected_title),
    body = stringResource(id = R.string.tk_global_error_unexpected_message),
    errorCode = errorCode,
    primaryButton = stringResource(id = R.string.global_error_backToHome_button),
    onPrimaryClick = onClose,
)

@Composable
@WalletAllScreenPreview
private fun EnableBiometricsErrorPreview() {
    WalletTheme {
        EnableBiometricsErrorContent(
            errorCode = "XY123-1234",
            onClose = {},
        )
    }
}
