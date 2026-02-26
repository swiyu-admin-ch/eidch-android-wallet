package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.SaveVcSdJwtCredentials
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import java.net.URL
import javax.inject.Inject

class HandleCredentialResultImpl @Inject constructor(
    private val saveVcSdJwtCredentials: SaveVcSdJwtCredentials,
) : HandleCredentialResult {
    override suspend fun invoke(
        issuerUrl: URL,
        anyVerifiedCredential: AnyVerifiedCredential,
        rawAndParsedCredentialInfo: RawAndParsedIssuerCredentialInfo,
        credentialConfig: AnyCredentialConfiguration,
    ): Result<FetchCredentialResult, FetchCredentialError> = coroutineBinding {
        val credentialId = saveVcSdJwtCredentials(
            issuerUrl = issuerUrl,
            vcSdJwtCredentials = listOf(anyVerifiedCredential.vcSdJwtCredential),
            rawAndParsedCredentialInfo = rawAndParsedCredentialInfo,
            credentialConfig = credentialConfig,
        ).bind()
        FetchCredentialResult.Credential(credentialId)
    }
}
