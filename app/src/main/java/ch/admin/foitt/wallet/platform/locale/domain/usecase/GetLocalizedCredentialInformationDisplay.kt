package ch.admin.foitt.wallet.platform.locale.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.OidIssuerDisplay

interface GetLocalizedCredentialInformationDisplay {
    operator fun invoke(
        displays: List<OidIssuerDisplay>,
        preferredLocaleString: String? = null,
    ): OidIssuerDisplay?
}
