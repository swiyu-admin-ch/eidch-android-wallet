package ch.admin.foitt.openid4vc.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.VerifiableCredentialParams
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.GetVerifiableCredentialParamsError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import com.github.michaelbull.result.Result

interface GetVerifiableCredentialParams {
    @CheckResult
    suspend operator fun invoke(
        issuerCredentialInfo: IssuerCredentialInfo,
        credentialConfiguration: AnyCredentialConfiguration,
        credentialOffer: CredentialOffer,
    ): Result<VerifiableCredentialParams, GetVerifiableCredentialParamsError>
}
