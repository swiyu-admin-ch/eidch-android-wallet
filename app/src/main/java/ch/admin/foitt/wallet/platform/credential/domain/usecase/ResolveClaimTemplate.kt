package ch.admin.foitt.wallet.platform.credential.domain.usecase

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays

interface ResolveClaimTemplate {
    operator fun invoke(
        template: String,
        claims: List<CredentialClaimWithDisplays>,
        allIndices: List<Int> = emptyList(),
    ): String
}
