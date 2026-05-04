package ch.admin.foitt.wallet.platform.credential.presentation.mock

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialDisplayStatus
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.preview.ComposableWrapper
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimCluster
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimText

object CredentialMocks {

    val cardState01
        @Composable get() = CredentialCardState(
            credentialId = 0L,
            title = "Lernfahrausweis B",
            subtitle = "Max Mustermann",
            status = CredentialDisplayStatus.Valid,
            logo = painterResource(id = R.drawable.ic_swiss_cross_small),
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            borderColor = MaterialTheme.colorScheme.primaryContainer,
            isCredentialFromBetaIssuer = false,
            progressionState = VerifiableProgressionState.ACCEPTED,
        )

    private val cardState02
        @Composable get() = CredentialCardState(
            credentialId = 0L,
            title = "Lernfahrausweis A",
            subtitle = "Lilly Mustermann",
            status = CredentialDisplayStatus.Unknown,
            logo = painterResource(id = R.drawable.ic_swiss_cross_small),
            backgroundColor = Color(0xFF335588),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            borderColor = Color(0xFF335588),
            isCredentialFromBetaIssuer = false,
            progressionState = VerifiableProgressionState.ACCEPTED,
        )

    private val cardState03
        @Composable get() = CredentialCardState(
            credentialId = 0L,
            title = "Lernfahrausweis B",
            subtitle = "Max Mustermann with a very looong name that does not fit in the card",
            status = CredentialDisplayStatus.Suspended,
            logo = null,
            backgroundColor = Color(0xFF996644),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            borderColor = Color(0xFFBB9977),
            isCredentialFromBetaIssuer = false,
            progressionState = VerifiableProgressionState.ACCEPTED,
        )

    private val cardState04 @Composable get() = cardState01.copy(isCredentialFromBetaIssuer = true)
    private val cardState05 @Composable get() = cardState02.copy(isCredentialFromBetaIssuer = true)
    private val cardState06 @Composable get() = cardState03.copy(isCredentialFromBetaIssuer = true)

    private val cardState07
        @Composable get() = CredentialCardState(
            credentialId = 0L,
            title = "Credential name",
            subtitle = null,
            status = CredentialDisplayStatus.Valid,
            logo = null,
            backgroundColor = CredentialCardState.defaultCardColor,
            contentColor = Color.Black,
            borderColor = CredentialCardState.defaultCardColor,
            isCredentialFromBetaIssuer = true,
            progressionState = VerifiableProgressionState.ACCEPTED,
        )
    private val cardState08
        @Composable get() = CredentialCardState(
            credentialId = 0L,
            title = "Deferred Credential",
            subtitle = null,
            status = null,
            logo = painterResource(R.drawable.wallet_ic_dotted_cross),
            backgroundColor = Color(0xFFEE9922),
            contentColor = Color.DarkGray,
            borderColor = Color(0xFFBB9977),
            isCredentialFromBetaIssuer = false,
            deferredStatus = DeferredProgressionState.IN_PROGRESS,
        )

    private val cardState09
        @Composable get() = CredentialCardState(
            credentialId = 0L,
            title = "Unnaccepted Credential",
            subtitle = null,
            status = CredentialDisplayStatus.Valid,
            logo = painterResource(R.drawable.wallet_ic_dotted_cross),
            backgroundColor = Color(0xFFEE9922),
            contentColor = Color.DarkGray,
            borderColor = Color(0xFFBB9977),
            isCredentialFromBetaIssuer = false,
            deferredStatus = null,
            progressionState = VerifiableProgressionState.UNACCEPTED,
        )

    private val cardState10
        @Composable get() = CredentialCardState(
            credentialId = 0L,
            title = "Deferred Credential Rejected",
            subtitle = null,
            status = null,
            logo = painterResource(R.drawable.wallet_ic_dotted_cross),
            backgroundColor = Color(0xFFEE9922),
            contentColor = Color.DarkGray,
            borderColor = Color(0xFFBB9977),
            isCredentialFromBetaIssuer = false,
            deferredStatus = DeferredProgressionState.INVALID,
        )

    val cardStates by lazy {
        sequenceOf(
            ComposableWrapper { cardState01 },
            ComposableWrapper { cardState02 },
            ComposableWrapper { cardState03 },
            ComposableWrapper { cardState04 },
            ComposableWrapper { cardState05 },
            ComposableWrapper { cardState06 },
            ComposableWrapper { cardState07 },
            ComposableWrapper { cardState08 },
            ComposableWrapper { cardState09 },
            ComposableWrapper { cardState10 },
        )
    }

    val clusterList by lazy {
        listOf(
            CredentialClaimCluster(
                id = 1,
                order = 1,
                localizedLabel = "Personal data",
                parentId = null,
                items = mutableListOf(
                    CredentialClaimText(id = 1, localizedLabel = "First name", order = 1, value = "Max", isSensitive = true),
                    CredentialClaimText(id = 2, localizedLabel = "Last name", order = 2, value = "Mustermann", isSensitive = false),
                    CredentialClaimText(id = 3, localizedLabel = "Date of birth", order = 3, value = "01.01.1970", isSensitive = false),
                    CredentialClaimCluster(
                        id = 2,
                        order = 2,
                        localizedLabel = "Address",
                        parentId = 1,
                        items = mutableListOf(
                            CredentialClaimText(
                                id = 4,
                                localizedLabel = "Street",
                                order = 1,
                                value = "1 Ipsum Avenue",
                                isSensitive = true
                            ),
                            CredentialClaimText(id = 5, localizedLabel = "City", order = 2, value = "3000 Loremburg", isSensitive = false),
                        )
                    )
                )
            ),
            CredentialClaimCluster(
                id = 2,
                order = 2,
                localizedLabel = "",
                parentId = null,
                items = mutableListOf(
                    CredentialClaimText(id = 6, localizedLabel = "AHV", order = 1, value = "756.9217.0769.85", isSensitive = false),
                )
            )
        )
    }
}
