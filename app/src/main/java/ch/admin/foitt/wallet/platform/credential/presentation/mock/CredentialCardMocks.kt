package ch.admin.foitt.wallet.platform.credential.presentation.mock

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialDisplayStatus
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.wallet.platform.preview.ComposableWrapper
import ch.admin.foitt.wallet.theme.WalletTheme

object CredentialCardMocks {

    private val state1 @Composable get() = CredentialCardState(
        credentialId = 1L,
        title = "Lernfahrausweis B",
        subtitle = "Max Mustermann",
        status = CredentialDisplayStatus.Valid,
        logo = painterResource(id = R.drawable.wallet_ic_shield_cross),
        backgroundColor = Color(0xFF00A3E0),
        contentColor = WalletTheme.colorScheme.onPrimaryContainer,
        borderColor = WalletTheme.colorScheme.primaryContainer,
        isCredentialFromBetaIssuer = true,
    )

    private val state2 @Composable get() = CredentialCardState(
        credentialId = 2L,
        title = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore " +
            "et dolore magna aliquyam erat, sed diam voluptua.",
        subtitle = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore " +
            "et dolore magna aliquyam erat, sed diam voluptua.",
        status = CredentialDisplayStatus.Valid,
        logo = painterResource(id = R.drawable.wallet_ic_shield_cross),
        backgroundColor = Color(0xFF444444),
        contentColor = WalletTheme.colorScheme.onPrimaryContainer,
        borderColor = WalletTheme.colorScheme.primaryContainer,
        isCredentialFromBetaIssuer = false,
    )

    private val state3 @Composable get() = CredentialCardState(
        credentialId = 3L,
        title = "Lernfahrausweis B",
        subtitle = null,
        status = CredentialDisplayStatus.Valid,
        logo = painterResource(id = R.drawable.wallet_ic_shield_cross),
        backgroundColor = Color(0xFFFFFFFF),
        contentColor = WalletTheme.colorScheme.onPrimaryContainer,
        borderColor = WalletTheme.colorScheme.primaryContainer,
        isCredentialFromBetaIssuer = false,
    )

    private val state4 @Composable get() = CredentialCardState(
        credentialId = 4L,
        title = null,
        subtitle = null,
        status = CredentialDisplayStatus.Valid,
        logo = painterResource(id = R.drawable.wallet_ic_shield_cross),
        backgroundColor = Color(0xFFFFFF55),
        contentColor = WalletTheme.colorScheme.onPrimaryContainer,
        borderColor = WalletTheme.colorScheme.primaryContainer,
        isCredentialFromBetaIssuer = false,
    )

    private val state5 @Composable get() = CredentialCardState(
        credentialId = 5L,
        title = null,
        subtitle = null,
        status = CredentialDisplayStatus.Valid,
        logo = null,
        backgroundColor = Color(0xFF772277),
        contentColor = WalletTheme.colorScheme.onPrimaryContainer,
        borderColor = WalletTheme.colorScheme.primaryContainer,
        isCredentialFromBetaIssuer = false,
    )

    private val state6 @Composable get() = CredentialCardState(
        credentialId = 6L,
        title = "Credential name",
        subtitle = null,
        status = CredentialDisplayStatus.Valid,
        logo = null,
        backgroundColor = CredentialCardState.defaultCardColor,
        contentColor = Color.Black,
        borderColor = CredentialCardState.defaultCardColor,
        isCredentialFromBetaIssuer = true,
    )

    val state7 @Composable get() = CredentialCardState(
        credentialId = 7L,
        title = "Deferred Credential In Progress",
        subtitle = null,
        status = null,
        logo = null,
        backgroundColor = Color(0xFF772277),
        contentColor = WalletTheme.colorScheme.onPrimaryContainer,
        borderColor = WalletTheme.colorScheme.primaryContainer,
        isCredentialFromBetaIssuer = false,
        deferredStatus = DeferredProgressionState.IN_PROGRESS,
    )

    val state8 @Composable get() = CredentialCardState(
        credentialId = 7L,
        title = "Deferred Credential Invalid",
        subtitle = null,
        status = null,
        logo = null,
        backgroundColor = Color(0xFF772277),
        contentColor = WalletTheme.colorScheme.onPrimaryContainer,
        borderColor = WalletTheme.colorScheme.primaryContainer,
        isCredentialFromBetaIssuer = false,
        deferredStatus = DeferredProgressionState.INVALID,
    )

    val mocks = sequenceOf(
        ComposableWrapper { state1 },
        ComposableWrapper { state2 },
        ComposableWrapper { state3 },
        ComposableWrapper { state4 },
        ComposableWrapper { state5 },
        ComposableWrapper { state6 },
        ComposableWrapper { state7 },
        ComposableWrapper { state8 }
    )
}
