package ch.admin.foitt.wallet.platform.credential.presentation.adapter

import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.model.DeferredCredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState

interface GetCredentialCardState {
    suspend operator fun invoke(
        credentialDisplayData: CredentialDisplayData,
    ): CredentialCardState

    suspend operator fun invoke(
        credentialDisplayData: DeferredCredentialDisplayData,
    ): CredentialCardState
}
