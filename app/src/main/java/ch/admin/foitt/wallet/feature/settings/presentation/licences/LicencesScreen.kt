package ch.admin.foitt.wallet.feature.settings.presentation.licences

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.settings.presentation.composables.LicenseSettingsItem
import ch.admin.foitt.wallet.feature.settings.presentation.composables.SettingsCard
import ch.admin.foitt.wallet.feature.settings.presentation.licences.model.Library
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.addTopScaffoldPadding
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletListItems
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun LicencesScreen(viewModel: LicencesViewModel) {
    LicencesScreenContent(
        libraries = viewModel.libraries.collectAsStateWithLifecycle().value,
        licenseDialog = viewModel.licenseDialog.collectAsStateWithLifecycle().value,
        onLibraryClick = viewModel::onLibraryClick,
        onDismissDialog = viewModel::onDismissDialog,
    )
}

@Composable
private fun LicencesScreenContent(
    libraries: List<Library>?,
    licenseDialog: Library?,
    onLibraryClick: (Library) -> Unit,
    onDismissDialog: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WalletTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = Sizes.s02)
            .addTopScaffoldPadding()
            .horizontalSafeDrawing()
    ) {
        libraries?.let {
            Licences(
                libraries = it,
                onLibraryClick = onLibraryClick,
            )
        }
        LoadingOverlay(libraries == null)

        licenseDialog?.let { library ->
            LicenseDialog(
                library = library,
                onDismissDialog = onDismissDialog,
            )
        }
    }
}

@Composable
private fun Licences(
    libraries: List<Library>,
    onLibraryClick: (Library) -> Unit,
) = WalletLayouts.LazyColumn(
    modifier = Modifier
) {
    item {
        SettingsCard(
            modifier = Modifier.padding(Sizes.s04)
        ) {
            WalletTexts.BodyMedium(
                text = stringResource(id = R.string.tk_settings_licences_body),
                color = WalletTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(Sizes.s04))
    }

    item {
        SettingsCard {
            libraries.forEachIndexed { index, library ->
                WalletListItems.LicenseSettingsItem(
                    title = library.name,
                    version = library.version,
                    onLibraryClick = { onLibraryClick(library) },
                )
                if (index != libraries.indices.last) {
                    WalletListItems.Divider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LicenseDialog(
    library: Library,
    onDismissDialog: () -> Unit,
) = AlertDialog(
    onDismissRequest = onDismissDialog,
    confirmButton = {
        Buttons.Text(
            text = stringResource(R.string.tk_global_close),
            onClick = onDismissDialog,
        )
    },
    modifier = Modifier,
    text = {
        Column(
            modifier = Modifier
                .padding(Sizes.s04)
                .verticalScroll(rememberScrollState()),
        ) {
            WalletTexts.BodyLarge(
                text = library.name,
                color = WalletTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(Sizes.s01))
            library.version?.let {
                WalletTexts.BodyLarge(
                    text = it,
                    color = WalletTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(Sizes.s01))
            }
            WalletTexts.BodyLarge(
                text = library.licenseName,
                color = WalletTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Sizes.s06))
            WalletTexts.BodyMedium(
                text = library.licenseContent,
                color = WalletTheme.colorScheme.onSurfaceVariant,
            )
        }
    },
)

@WalletAllScreenPreview
@Composable
fun LicencesScreenPreview() {
    WalletTheme {
        LicencesScreenContent(
            libraries = listOf(
                Library(name = "Lib 1", version = "1.2.3", licenseName = "Apache 2.0", licenseContent = "Lorem ipsum"),
                Library(name = "Lib 2", version = "1.2.3", licenseName = "Apache 2.0", licenseContent = "Lorem ipsum"),
                Library(name = "Lib 3", version = "1.2.3", licenseName = "Apache 2.0", licenseContent = "Lorem ipsum"),
                Library(name = "Lib 4", version = "1.2.3", licenseName = "Apache 2.0", licenseContent = "Lorem ipsum"),
            ),
            licenseDialog = Library(name = "Lib 1", version = "1.2.3", licenseName = "Apache 2.0", licenseContent = "Lorem ipsum"),
            onLibraryClick = {},
            onDismissDialog = {}
        )
    }
}
