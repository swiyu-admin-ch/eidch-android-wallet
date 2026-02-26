package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetCredentialConfig
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import javax.inject.Inject

internal class GetCredentialConfigImpl @Inject constructor() : GetCredentialConfig {
    override suspend fun invoke(
        credentials: List<String>,
        credentialConfigurations: List<AnyCredentialConfiguration>
    ): Result<AnyCredentialConfiguration, FetchCredentialError> {
        val matchingCredentials = credentialConfigurations.filter { it.identifier in credentials }
        return if (matchingCredentials.isEmpty()) {
            Err(CredentialError.UnsupportedCredentialIdentifier)
        } else {
            Ok(matchingCredentials.first())
        }
    }
}
