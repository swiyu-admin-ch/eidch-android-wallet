package ch.admin.foitt.wallet.platform.credential.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import com.github.michaelbull.result.Result

interface GetCredentialConfig {
    @CheckResult
    suspend operator fun invoke(
        credentials: List<String>,
        credentialConfigurations: List<AnyCredentialConfiguration>
    ): Result<AnyCredentialConfiguration, FetchCredentialError>
}
