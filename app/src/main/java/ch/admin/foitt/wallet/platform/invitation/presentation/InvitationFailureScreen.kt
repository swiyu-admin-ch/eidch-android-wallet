package ch.admin.foitt.wallet.platform.invitation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.ErrorScreenContent
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationErrorScreenState
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun InvitationFailureScreen(
    viewModel: InvitationFailureViewModel,
) {
    InvitationFailureScreenContent(
        screenState = viewModel.invitationErrorScreenState,
        onClose = viewModel::close,
    )
}

@Composable
private fun InvitationFailureScreenContent(
    screenState: InvitationErrorScreenState,
    onClose: () -> Unit,
) = when (screenState) {
    InvitationErrorScreenState.INVALID_CREDENTIAL -> InvalidCredential(onClose)
    InvitationErrorScreenState.UNKNOWN_ISSUER -> UnknownIssuer(onClose)
    InvitationErrorScreenState.INVALID_PRESENTATION -> InvalidPresentation(onClose)
    InvitationErrorScreenState.EMPTY_WALLET,
    InvitationErrorScreenState.NO_COMPATIBLE_CREDENTIAL -> NoCompatibleCredential(onClose)
    InvitationErrorScreenState.UNSUPPORTED_KEY_STORAGE -> UnsupportedKeyStorageSecurityLevel(onClose)
    InvitationErrorScreenState.UNSUPPORTED_KEY_STORAGE_CAPABILITIES -> IncompatibleDeviceKeyStorage(onClose)
    InvitationErrorScreenState.NETWORK_ERROR -> NetworkError(onClose)
    InvitationErrorScreenState.UNEXPECTED -> UnexpectedError(onClose)
}

@Composable
private fun InvalidCredential(onClose: () -> Unit) = ErrorScreenContent(
    iconRes = R.drawable.wallet_ic_error_credential,
    title = stringResource(id = R.string.tk_error_invitationcredential_title),
    body = stringResource(id = R.string.tk_error_invitationcredential_body),
    primaryButton = stringResource(id = R.string.tk_global_close),
    onPrimaryClick = onClose,
)

@Composable
private fun UnknownIssuer(onClose: () -> Unit) = ErrorScreenContent(
    iconRes = R.drawable.wallet_ic_error_questionmark,
    title = stringResource(id = R.string.tk_error_issuer_notregistered_title),
    body = stringResource(id = R.string.tk_error_issuer_notregistered_body),
    primaryButton = stringResource(id = R.string.tk_global_close),
    onPrimaryClick = onClose,
)

@Composable
private fun InvalidPresentation(onClose: () -> Unit) = ErrorScreenContent(
    iconRes = R.drawable.wallet_ic_error_questionmark,
    title = stringResource(R.string.tk_error_invalidrequest_title),
    body = stringResource(R.string.tk_error_invalidrequest_body),
    primaryButton = stringResource(id = R.string.tk_global_close),
    onPrimaryClick = onClose,
)

@Composable
private fun NoCompatibleCredential(onClose: () -> Unit) = ErrorScreenContent(
    iconRes = R.drawable.wallet_ic_error_credential,
    title = stringResource(R.string.tk_present_credentialNotFound_title),
    body = stringResource(R.string.tk_present_credentialNotFound_body),
    primaryButton = stringResource(id = R.string.tk_global_close),
    onPrimaryClick = onClose,
)

@Composable
private fun UnsupportedKeyStorageSecurityLevel(onClose: () -> Unit) = ErrorScreenContent(
    iconRes = R.drawable.wallet_ic_error_general,
    title = stringResource(R.string.tk_error_keyStorageUnsupported_title),
    body = stringResource(R.string.tk_error_keyStorageUnsupported_body),
    primaryButton = stringResource(id = R.string.tk_global_close),
    onPrimaryClick = onClose,
)

@Composable
private fun IncompatibleDeviceKeyStorage(onClose: () -> Unit) = ErrorScreenContent(
    iconRes = R.drawable.wallet_ic_error_general,
    title = stringResource(R.string.tk_error_strongboxUnavailable_title),
    body = stringResource(R.string.tk_error_strongboxUnavailable_body),
    primaryButton = stringResource(id = R.string.tk_global_close),
    onPrimaryClick = onClose,
)

@Composable
private fun NetworkError(onClose: () -> Unit) = ErrorScreenContent(
    iconRes = R.drawable.wallet_ic_error_network,
    title = stringResource(id = R.string.tk_error_connectionproblem_title),
    body = stringResource(id = R.string.tk_error_connectionproblem_body),
    primaryButton = stringResource(id = R.string.tk_global_close),
    onPrimaryClick = onClose,
)

@Composable
private fun UnexpectedError(onClose: () -> Unit) = ErrorScreenContent(
    iconRes = R.drawable.wallet_ic_error_general,
    title = stringResource(id = R.string.tk_global_error_unexpected_title),
    body = stringResource(id = R.string.tk_global_error_unexpected_message),
    primaryButton = stringResource(id = R.string.tk_global_close),
    onPrimaryClick = onClose,
)

private class InvitationFailureParams : PreviewParameterProvider<InvitationErrorScreenState> {
    override val values: Sequence<InvitationErrorScreenState> = sequenceOf(
        InvitationErrorScreenState.INVALID_CREDENTIAL,
        InvitationErrorScreenState.INVALID_PRESENTATION,
        InvitationErrorScreenState.EMPTY_WALLET,
        InvitationErrorScreenState.NO_COMPATIBLE_CREDENTIAL,
        InvitationErrorScreenState.UNSUPPORTED_KEY_STORAGE,
        InvitationErrorScreenState.UNSUPPORTED_KEY_STORAGE_CAPABILITIES,
        InvitationErrorScreenState.NETWORK_ERROR,
        InvitationErrorScreenState.UNKNOWN_ISSUER,
        InvitationErrorScreenState.UNEXPECTED,
    )
}

@WalletAllScreenPreview
@Composable
private fun InvitationFailureScreenPreview(
    @PreviewParameter(InvitationFailureParams::class) screenState: InvitationErrorScreenState,
) {
    WalletTheme {
        InvitationFailureScreenContent(
            screenState = screenState,
            onClose = {},
        )
    }
}
