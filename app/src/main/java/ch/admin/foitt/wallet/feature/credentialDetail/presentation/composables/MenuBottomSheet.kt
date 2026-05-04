package ch.admin.foitt.wallet.feature.credentialDetail.presentation.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onWrongData: (() -> Unit)? = null,
) {
    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = WalletTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier.semantics {
            isTraversalGroup = true
            traversalIndex = -5f
        }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Sizes.s04)
        ) {
            onWrongData?.let { callback ->
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = WalletTheme.colorScheme.surfaceContainerHighest),
                    modifier = Modifier
                        .clickable(onClick = callback)
                        .spaceBarKeyClickable(callback)
                        .semantics {
                            role = Role.Button
                        },
                    leadingContent = {
                        Icon(
                            modifier = Modifier.width(18.dp).height(18.dp),
                            painter = painterResource(R.drawable.wallet_ic_wrong_data),
                            tint = WalletTheme.colorScheme.onBackground,
                            contentDescription = null,
                        )
                    },
                    headlineContent = {
                        WalletTexts.BodyLarge(
                            text = stringResource(id = R.string.tk_global_wrongdata),
                            color = WalletTheme.colorScheme.onBackground,
                        )
                    },
                )
            }
            ListItem(
                colors = ListItemDefaults.colors(containerColor = WalletTheme.colorScheme.surfaceContainerHighest),
                modifier = Modifier
                    .clickable(onClick = onDelete)
                    .spaceBarKeyClickable(onDelete)
                    .semantics {
                        role = Role.Button
                    },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.wallet_ic_trashcan),
                        tint = WalletTheme.colorScheme.onLightError,
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    WalletTexts.BodyLarge(
                        text = stringResource(id = R.string.tk_displaydelete_credentialmenu_primarybutton),
                        color = WalletTheme.colorScheme.onLightError,
                    )
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@WalletComponentPreview
@Composable
private fun CredentialDetailBottomSheetContentPreview() {
    WalletTheme {
        MenuBottomSheet(
            sheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.Expanded
            ),
            onDismiss = {},
            onDelete = {},
            onWrongData = {}
        )
    }
}
