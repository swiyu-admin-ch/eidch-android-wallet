package ch.admin.foitt.wallet.feature.settings.presentation.security.activityHistory

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.platform.utils.TraversalIndex
import ch.admin.foitt.wallet.platform.utils.setIsTraversalGroup
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletButtonColors
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteHistoryBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = WalletTheme.colorScheme.surface,
        modifier = Modifier.setIsTraversalGroup(
            isTraversalGroup = true,
            index = TraversalIndex.HIGH1,
        )
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Sizes.s04)
        ) {
            Spacer(modifier = Modifier.height(Sizes.s04))
            WalletTexts.HeadlineMedium(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Sizes.s06),
                text = stringResource(R.string.tk_settings_activityHistory_deletion_confirmationPrimary),
                color = WalletTheme.colorScheme.onSurface,
            )
            WalletTexts.BodyLarge(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Sizes.s06, end = Sizes.s06, top = Sizes.s06),
                text = stringResource(R.string.tk_settings_activityHistory_deletion_confirmationSecondary),
                color = WalletTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Sizes.s04))
            Buttons.Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.tk_global_delete),
                colors = WalletButtonColors.textError().copy(contentColor = WalletTheme.colorScheme.onLightError),
                onClick = onDelete,
            )
            Spacer(modifier = Modifier.height(Sizes.s04))
            Buttons.Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.global_cancel),
                onClick = onCancel,
            )
            Spacer(modifier = Modifier.height(Sizes.s04))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@WalletComponentPreview
@Composable
private fun SaveHistoryBottomSheetContentPreview() {
    WalletTheme {
        DeleteHistoryBottomSheet(
            sheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.Expanded
            ),
            onDismiss = {},
            onDelete = {},
            onCancel = {},
        )
    }
}
