package ch.admin.foitt.wallet.platform.credential.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyDeferredCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import com.github.michaelbull.result.Result
import java.net.URL

interface HandleDeferredCredentialResult {
    suspend operator fun invoke(
        issuerUrl: URL,
        deferredCredential: AnyDeferredCredential,
        rawAndParsedCredentialInfo: RawAndParsedIssuerCredentialInfo,
        credentialConfig: AnyCredentialConfiguration,
    ): Result<FetchCredentialResult, FetchCredentialError>
}
