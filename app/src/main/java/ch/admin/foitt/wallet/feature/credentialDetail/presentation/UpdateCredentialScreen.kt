package ch.admin.foitt.wallet.feature.credentialDetail.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.clusterLazyListItem
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialIssuer
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.scaffold.presentation.LocalScaffoldPaddings
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun UpdateCredentialScreen(viewModel: UpdateCredentialViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    UpdateCredentialScreenContent(
        issuerName = uiState.issuerName,
        issuerPainter = uiState.issuerPainter,
    )
}

@Composable
private fun UpdateCredentialScreenContent(
    issuerName: String?,
    issuerPainter: Painter?,
) = Box(
    modifier = Modifier
        .fillMaxSize()
        .background(color = WalletTheme.colorScheme.surfaceContainerLow)
) {
    val buttonHeight = remember { mutableStateOf(0.dp) }

    WalletLayouts.LazyColumn(
        modifier = Modifier
            .widthIn(max = Sizes.contentMaxWidth)
            .horizontalSafeDrawing()
            .align(Alignment.TopCenter),
        state = rememberLazyListState(),
        contentPadding = PaddingValues(top = Sizes.s02, bottom = Sizes.s04),
        useTopInsets = false,
    ) {
        item {
            WalletLayouts.TopInsetSpacer(
                shouldScrollUnderTopBar = true,
                scaffoldPaddings = LocalScaffoldPaddings.current,
            )
        }

        item { Spacer(modifier = Modifier.height(Sizes.s02)) }

        updateCredentialInfoListItem()

        item { Spacer(modifier = Modifier.height(Sizes.s02)) }

        item {
            CredentialIssuer(
                issuer = issuerName,
                issuerIcon = issuerPainter,
            )
        }

        item { Spacer(modifier = Modifier.height(buttonHeight.value)) }
    }
}

private fun LazyListScope.updateCredentialInfoListItem() {
    clusterLazyListItem(
        isFirstItem = true,
        isLastItem = true,
        paddingValues = PaddingValues(horizontal = Sizes.s04)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Sizes.s04, vertical = Sizes.s06),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(Sizes.s01)
        ) {
            Image(
                modifier = Modifier
                    .size(Sizes.s16)
                    .padding(Sizes.s02),
                painter = painterResource(R.drawable.wallet_ic_update_credential),
                contentDescription = null,
            )

            WalletTexts.TitleMedium(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { heading() },
                text = stringResource(R.string.tk_displayrefresh_title),
                textAlign = TextAlign.Start,
            )

            WalletTexts.BodyMedium(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.tk_displayrefresh_body),
                textAlign = TextAlign.Start,
            )
        }
    }
}

@WalletAllScreenPreview
@Composable
private fun UpdateCredentialScreenPreview() {
    WalletTheme {
        UpdateCredentialScreenContent(
            issuerName = "Issuer01",
            issuerPainter = painterResource(id = R.drawable.wallet_ic_actor_default)
        )
    }
}
