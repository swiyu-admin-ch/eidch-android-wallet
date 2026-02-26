@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.feature.qrscan.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Toast

@Composable
fun QrToastHint(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
) = QrInfoToast(
    modifier = modifier,
    headline = R.string.tk_qrscanner_scanning_title,
    text = R.string.tk_qrscanner_scanning_body,
    iconStart = R.drawable.wallet_ic_qr,
    onClose = onClose,
)

@Composable
fun QrToastInvalidQr(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
) = QrInfoToast(
    modifier = modifier,
    headline = R.string.tk_error_invalidqrcode_title,
    text = R.string.tk_error_invalidqrcode_body,
    iconStart = R.drawable.wallet_ic_qr,
    onClose = onClose,
)

@Composable
fun QrToastInvalidCredentialOffer(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
) = QrInfoToast(
    modifier = modifier,
    headline = R.string.tk_error_invitationcredential_title,
    text = R.string.tk_error_invitationcredential_body,
    iconStart = R.drawable.wallet_ic_toast_credential,
    onClose = onClose,
)

@Composable
fun QrToastUnknownIssuer(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
) = QrInfoToast(
    modifier = modifier,
    headline = R.string.tk_error_issuer_notregistered_title,
    text = R.string.tk_error_issuer_notregistered_body,
    iconStart = R.drawable.wallet_ic_questionmark,
    onClose = onClose,
)

@Composable
fun QrToastUnknownVerifier(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
) = QrInfoToast(
    modifier = modifier,
    headline = R.string.tk_error_verifier_notregistered_title,
    text = R.string.tk_error_verifier_notregistered_body,
    iconStart = R.drawable.wallet_ic_questionmark,
    onClose = onClose,
)

@Composable
fun QrToastExpiredCredentialOffer(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
) = QrInfoToast(
    modifier = modifier,
    headline = R.string.tk_error_notusable_title,
    text = R.string.tk_error_notusable_body,
    iconStart = R.drawable.wallet_ic_qr,
    onClose = onClose,
)

@Composable
fun QrToastNetworkError(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
) = QrInfoToast(
    modifier = modifier,
    headline = R.string.tk_error_connectionproblem_title,
    text = R.string.tk_error_connectionproblem_body,
    iconStart = R.drawable.wallet_ic_wifi_crossed,
    onClose = onClose,
)

@Composable
fun QrToastInvalidPresentation(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
) = QrInfoToast(
    modifier = modifier,
    headline = R.string.tk_error_invalidrequest_title,
    text = R.string.tk_error_invalidrequest_body,
    iconStart = R.drawable.wallet_ic_questionmark,
    onClose = onClose,
)

@Composable
fun QrToastUnsupportedKeyStorageSecurityLevel(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
) = QrInfoToast(
    modifier = modifier,
    headline = R.string.tk_error_keyStorageUnsupported_title,
    text = R.string.tk_error_keyStorageUnsupported_body,
    iconStart = R.drawable.wallet_ic_toast_error,
    onClose = onClose,
)

@Composable
fun QrToastIncompatibleDeviceKeyStorage(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
) = QrInfoToast(
    modifier = modifier,
    headline = R.string.tk_error_strongboxUnavailable_title,
    text = R.string.tk_error_strongboxUnavailable_body,
    iconStart = R.drawable.wallet_ic_toast_error,
    onClose = onClose,
)

@Composable
private fun QrInfoToast(
    modifier: Modifier = Modifier,
    @StringRes headline: Int,
    @StringRes text: Int,
    @DrawableRes iconStart: Int,
    onClose: () -> Unit,
) = Toast(
    modifier = modifier,
    headline = headline,
    text = text,
    linkText = null,
    iconEndContentDescription = R.string.qrScanner_toast_close_button,
    iconStart = iconStart,
    iconEnd = R.drawable.wallet_ic_cross,
    onLink = { },
    onIconEnd = onClose,
)
