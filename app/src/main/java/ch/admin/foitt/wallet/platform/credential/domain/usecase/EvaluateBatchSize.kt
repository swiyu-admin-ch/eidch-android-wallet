package ch.admin.foitt.wallet.platform.credential.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.BatchSize
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import com.github.michaelbull.result.Result

interface EvaluateBatchSize {
    operator fun invoke(
        issuerCredentialInfo: IssuerCredentialInfo,
    ): Result<BatchSize, CredentialError.InvalidIssuerCredentialInfo>
}
