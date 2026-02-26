package ch.admin.foitt.wallet.platform.scaffold.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.platform.scaffold.domain.model.ErrorDialogState
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun ErrorDialog(
    viewModel: ErrorDialogViewModel = hiltViewModel()
) {
    when (val state = viewModel.state.collectAsStateWithLifecycle().value) {
        ErrorDialogState.Closed -> {}
        is ErrorDialogState.Unexpected -> ErrorDialogContent(
            title = stringResource(id = R.string.tk_global_error_unexpected_title),
            message = stringResource(id = R.string.tk_global_error_unexpected_message),
            details = state.errorDetails,
            buttonText = stringResource(id = R.string.global_error_confirm_button),
            icon = painterResource(id = R.drawable.wallet_ic_circular_cross),
            onDismiss = viewModel::onDismiss,
        )

        is ErrorDialogState.Network -> ErrorDialogContent(
            title = stringResource(id = R.string.tk_global_error_network_title),
            message = stringResource(id = R.string.tk_global_error_network_message),
            details = state.errorDetails,
            buttonText = stringResource(id = R.string.global_error_confirm_button),
            icon = painterResource(id = R.drawable.wallet_ic_wifi),
            onDismiss = viewModel::onDismiss,
        )

        is ErrorDialogState.Wallet -> ErrorDialogContent(
            title = stringResource(id = R.string.global_error_wallet_title),
            message = stringResource(id = R.string.global_error_wallet_message),
            details = state.errorDetails,
            buttonText = stringResource(id = R.string.global_error_confirm_button),
            icon = painterResource(id = R.drawable.wallet_ic_circular_cross),
            onDismiss = viewModel::onDismiss,
        )
    }
}

@Composable
private fun ErrorDialogContent(
    title: String,
    message: String,
    details: String?,
    icon: Painter,
    buttonText: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Text(
                text = buttonText,
                modifier = Modifier
                    .padding(start = Sizes.s04, top = Sizes.s02, bottom = Sizes.s02)
                    .clickable {
                        onDismiss()
                    },
            )
        },
        modifier = Modifier,
        icon = {
            Image(
                modifier = Modifier.size(48.dp),
                painter = icon,
                contentDescription = title,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error),
            )
        },
        title = {
            Text(text = title)
        },
        text = {
            Column {
                Text(text = message)
                details?.let { detailsString ->
                    Spacer(modifier = Modifier.height(Sizes.s02))
                    Text(text = detailsString)
                }
            }
        },
    )
}

@Composable
@WalletComponentPreview
private fun ErrorDialogPreview() {
    WalletTheme {
        ErrorDialogContent(
            title = stringResource(id = R.string.tk_global_error_unexpected_title),
            message = stringResource(id = R.string.tk_global_error_unexpected_message),
            details = "WalletBustedException",
            icon = painterResource(id = R.drawable.wallet_ic_circular_cross),
            buttonText = stringResource(id = R.string.global_error_confirm_button),
            onDismiss = {},
        )
    }
}
