package ch.admin.foitt.wallet.platform.credential.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchExistingIssuerCredentialInfoError
import com.github.michaelbull.result.Result

fun interface FetchExistingIssuerCredentialInfo {
    suspend operator fun invoke(credentialId: Long): Result<RawAndParsedIssuerCredentialInfo, FetchExistingIssuerCredentialInfoError>
}
