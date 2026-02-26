package ch.admin.foitt.wallet.platform.credential.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo

interface ValidateIssuerCredentialInfo {
    operator fun invoke(issuerCredentialInfo: IssuerCredentialInfo): Boolean
}
