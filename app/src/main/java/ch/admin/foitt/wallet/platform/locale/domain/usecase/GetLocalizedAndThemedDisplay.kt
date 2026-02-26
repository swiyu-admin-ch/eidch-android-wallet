package ch.admin.foitt.wallet.platform.locale.domain.usecase

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.theme.domain.model.Theme

interface GetLocalizedAndThemedDisplay {
    operator fun invoke(
        credentialDisplays: List<CredentialDisplay>,
        preferredLocaleString: String? = null,
        preferredTheme: Theme = Theme.LIGHT,
    ): CredentialDisplay?
}
