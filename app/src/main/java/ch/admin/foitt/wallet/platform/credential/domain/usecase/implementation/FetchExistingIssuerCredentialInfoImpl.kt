package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerCredentialInfoError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.usecase.FetchRawAndParsedIssuerCredentialInfo
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchExistingIssuerCredentialInfoError
import ch.admin.foitt.wallet.platform.credential.domain.model.toFetchExistingIssuerCredentialInfoError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchExistingIssuerCredentialInfo
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialRepo
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class FetchExistingIssuerCredentialInfoImpl @Inject constructor(
    private val credentialRepository: CredentialRepo,
    private val fetchRawAndParsedIssuerCredentialInfo: FetchRawAndParsedIssuerCredentialInfo,
) : FetchExistingIssuerCredentialInfo {
    override suspend fun invoke(credentialId: Long): Result<RawAndParsedIssuerCredentialInfo, FetchExistingIssuerCredentialInfoError> =
        coroutineBinding {
            val credential = credentialRepository.getById(credentialId)
                .mapError(CredentialRepositoryError::toFetchExistingIssuerCredentialInfoError)
                .bind()

            fetchRawAndParsedIssuerCredentialInfo(
                issuerEndpoint = credential.issuerUrl,
            ).mapError(FetchIssuerCredentialInfoError::toFetchExistingIssuerCredentialInfoError).bind()
        }
}
