package ch.admin.foitt.openid4vc.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerConfigurationError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerConfiguration
import com.github.michaelbull.result.Result
import java.net.URL

interface FetchIssuerConfiguration {
    @CheckResult
    suspend operator fun invoke(
        issuerEndpoint: URL
    ): Result<IssuerConfiguration, FetchIssuerConfigurationError>
}
