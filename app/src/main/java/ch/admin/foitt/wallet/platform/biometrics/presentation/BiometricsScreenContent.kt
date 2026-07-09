package ch.admin.foitt.wallet.platform.biometrics.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun BiometricsContent(
    @StringRes title: Int,
    @StringRes description: Int,
    @StringRes infoText: Int?,
) {
    Spacer(modifier = Modifier.height(Sizes.s04))
    WalletTexts.TitleScreen(text = stringResource(id = title))
    Spacer(modifier = Modifier.height(Sizes.s04))
    WalletTexts.Body(
        modifier = Modifier
            .fillMaxWidth(),
        text = stringResource(id = description),
        color = WalletTheme.colorScheme.secondary,
    )
    infoText?.let {
        Spacer(
            modifier = Modifier
                .height(Sizes.s04)
        )
        WalletTexts.LabelSmall(
            text = stringResource(id = infoText)
        )
    }
}
