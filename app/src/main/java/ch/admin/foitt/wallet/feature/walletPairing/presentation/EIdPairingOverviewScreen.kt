package ch.admin.foitt.wallet.feature.walletPairing.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.walletPairing.presentation.model.PairingMainWalletUiState
import ch.admin.foitt.wallet.feature.walletPairing.presentation.model.PairingOtherWalletUiState
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.ToastAnimated
import ch.admin.foitt.wallet.platform.composables.presentation.HeightReportingLayout
import ch.admin.foitt.wallet.platform.composables.presentation.addTopScaffoldPadding
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.verticalSafeDrawing
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.OnResumeEventHandler
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun EIdPairingOverviewScreen(
    viewModel: EIdPairingOverviewViewModel,
) {
    OnResumeEventHandler {
        viewModel.onResume()
    }

    EIdParingOverviewScreenContent(
        otherWalletUiState = viewModel.otherWalletUiState.collectAsStateWithLifecycle().value,
        mainWalletUiState = viewModel.mainWalletUiState.collectAsStateWithLifecycle().value,
        onThisDeviceClick = viewModel::onThisDeviceClick,
        onAdditionalDevicesClick = viewModel::onAdditionalDevicesClick,
        onContinueClick = viewModel::onContinue,
        deviceName = viewModel.deviceName,
        numberOfDevices = viewModel.numberOfDevices.collectAsStateWithLifecycle().value,
        isToastVisible = viewModel.isToastVisible.collectAsStateWithLifecycle(false).value,
        dateAddedText = viewModel.dateAdded.collectAsStateWithLifecycle(null).value,
    )
}

@Composable
private fun EIdParingOverviewScreenContent(
    otherWalletUiState: PairingOtherWalletUiState,
    mainWalletUiState: PairingMainWalletUiState,
    onThisDeviceClick: () -> Unit,
    onAdditionalDevicesClick: () -> Unit,
    onContinueClick: () -> Unit = {},
    deviceName: String,
    numberOfDevices: Int = 0,
    isToastVisible: Boolean,
    dateAddedText: String?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        var buttonHeight by remember { mutableStateOf(0.dp) }

        OverviewList(
            otherWalletUiState = otherWalletUiState,
            mainWalletUiState = mainWalletUiState,
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = Sizes.s06),
            onThisDeviceClick = onThisDeviceClick,
            onAdditionalDevicesClick = onAdditionalDevicesClick,
            deviceName = deviceName,
            numberOfDevices = numberOfDevices,
            buttonHeight = buttonHeight,
            dateAddedText = dateAddedText
        )
        HeightReportingLayout(
            modifier = Modifier.align(Alignment.BottomCenter),
            onContentHeightMeasured = { height -> buttonHeight = height },
        ) {
            Buttons.FilledPrimary(
                text = stringResource(id = R.string.tk_eidRequest_walletPairing_button_primary),
                onClick = onContinueClick,
                enabled = numberOfDevices > 0 || dateAddedText != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalSafeDrawing()
                    .horizontalSafeDrawing()
                    .padding(horizontal = Sizes.s04, vertical = Sizes.s04)
            )
        }
    }
    ToastAnimated(
        isVisible = isToastVisible,
        isSnackBarDesign = false,
        messageToast = R.string.tk_eidRequest_walletPairing_notification_success,
        contentBottomPadding = Sizes.s24
    )
}

@Composable
private fun OverviewList(
    otherWalletUiState: PairingOtherWalletUiState,
    mainWalletUiState: PairingMainWalletUiState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onThisDeviceClick: () -> Unit,
    onAdditionalDevicesClick: () -> Unit,
    deviceName: String,
    numberOfDevices: Int,
    buttonHeight: Dp,
    dateAddedText: String?,
) {
    WalletLayouts.LazyColumn(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        contentPadding = contentPadding,
    ) {
        item {
            ListHeader()
        }
        item {
            Sections(text = R.string.tk_eidRequest_walletPairing_currentDevice_sectionTitle)
        }
        item {
            EIdDeviceItem(
                onClick = { onThisDeviceClick() },
                title = stringResource(R.string.tk_eidRequest_walletPairing_currentDevice_button_primary),
                subtitle = deviceName,
                dateAddedText = dateAddedText,
                mainWalletUiState = mainWalletUiState
            )
        }
        item {
            Sections(text = R.string.tk_eidRequest_walletPairing_additionalDevice_sectionTitle)
        }

        if (numberOfDevices > 0) {
            item {
                EIdOtherDeviceItem(
                    title = pluralStringResource(
                        R.plurals.tk_eidRequest_walletPairing_additionalDevice_counter,
                        numberOfDevices,
                        numberOfDevices
                    ),
                )
            }
        }

        when (otherWalletUiState) {
            PairingOtherWalletUiState.Open -> item {
                EIdDeviceItem(
                    onClick = { onAdditionalDevicesClick() },
                    title = stringResource(R.string.tk_eidRequest_walletPairing_additionalDevice_button_primary),
                )
            }
            PairingOtherWalletUiState.LimitReached -> item {
                ListBottom()
            }
        }

        item {
            Spacer(modifier = Modifier.height(buttonHeight))
        }
    }
}

@Composable
private fun ListHeader() {
    Column(
        modifier = Modifier
            .addTopScaffoldPadding()
            .padding(
                start = Sizes.s04,
                end = Sizes.s04,
            )
    ) {
        WalletTexts.TitleScreen(
            text = stringResource(id = R.string.tk_eidRequest_walletPairing_primary)
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.BodyLarge(
            text = stringResource(id = R.string.tk_eidRequest_walletPairing_secondary)
        )
    }
}

@Composable
private fun ListBottom() {
    Column(
        modifier = Modifier
            .padding(
                top = Sizes.s03,
                start = Sizes.s04,
                end = Sizes.s04,
                bottom = Sizes.s03
            )
    ) {
        WalletTexts.BodyMedium(
            text = stringResource(id = R.string.tk_eidRequest_walletPairing_additionalDevice_sectionFooter),
            color = WalletTheme.colorScheme.secondary,
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = Sizes.s04)
    )
}

@Composable
private fun Sections(
    modifier: Modifier = Modifier,
    @StringRes text: Int
) {
    Row(
        modifier = modifier
            .padding(start = Sizes.s04, top = Sizes.s06, end = Sizes.s06, bottom = Sizes.s03),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WalletTexts.TitleMedium(
            modifier = Modifier.semantics { heading() },
            text = stringResource(id = text),
            color = WalletTheme.colorScheme.onSurface,
        )
    }
}

@WalletAllScreenPreview
@Composable
private fun EIdParingOverviewScreenPreview() {
    WalletTheme {
        EIdParingOverviewScreenContent(
            otherWalletUiState = PairingOtherWalletUiState.Open,
            mainWalletUiState = PairingMainWalletUiState.SyncMainWallet,
            onThisDeviceClick = {},
            onAdditionalDevicesClick = {},
            deviceName = "Google Pixel 10",
            numberOfDevices = 3,
            isToastVisible = true,
            dateAddedText = null
        )
    }
}
