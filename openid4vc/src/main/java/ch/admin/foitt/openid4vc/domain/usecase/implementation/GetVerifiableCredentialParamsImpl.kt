package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.VerifiableCredentialParams
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerConfigurationError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerCredentialInfoError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.GetVerifiableCredentialParamsError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.ProofType
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.toGetVerifiableCredentialParamsError
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository
import ch.admin.foitt.openid4vc.domain.usecase.FetchIssuerConfiguration
import ch.admin.foitt.openid4vc.domain.usecase.GetVerifiableCredentialParams
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

internal class GetVerifiableCredentialParamsImpl @Inject constructor(
    private val credentialOfferRepository: CredentialOfferRepository,
    private val fetchIssuerConfiguration: FetchIssuerConfiguration,
) : GetVerifiableCredentialParams {
    override suspend fun invoke(
        issuerCredentialInfo: IssuerCredentialInfo,
        credentialConfiguration: AnyCredentialConfiguration,
        credentialOffer: CredentialOffer,
    ): Result<VerifiableCredentialParams, GetVerifiableCredentialParamsError> = coroutineBinding {
        val issuerEndpoint = credentialOffer.credentialIssuer
        if (credentialConfiguration.identifier !in credentialOffer.credentialConfigurationIds) {
            return@coroutineBinding Err(CredentialOfferError.InvalidCredentialOffer).bind<VerifiableCredentialParams>()
        }

        credentialConfiguration.proofTypesSupported.let { proofTypes ->
            if (proofTypes.isNotEmpty() && proofTypes.keys.none { it == ProofType.JWT }) {
                return@coroutineBinding Err(CredentialOfferError.UnsupportedProofType).bind<VerifiableCredentialParams>()
            }

            credentialConfiguration.cryptographicBindingMethodsSupported?.let { bindingMethods ->
                if (bindingMethods.intersect(supportedBindingMethods).isEmpty()) {
                    return@coroutineBinding Err(
                        CredentialOfferError.UnsupportedCryptographicSuite
                    ).bind<VerifiableCredentialParams>()
                }
            }
        }

        val issuerConfig = fetchIssuerConfiguration(issuerEndpoint)
            .mapError(FetchIssuerConfigurationError::toGetVerifiableCredentialParamsError)
            .bind()

        val issuerInfo = credentialOfferRepository.getIssuerCredentialInfo(issuerEndpoint = issuerEndpoint)
            .mapError(FetchIssuerCredentialInfoError::toGetVerifiableCredentialParamsError)
            .bind()

        val proofTypeConfig = credentialConfiguration.proofTypesSupported.entries.firstOrNull {
            it.key == ProofType.JWT
        }?.value

        VerifiableCredentialParams(
            proofTypeConfig = proofTypeConfig,
            tokenEndpoint = issuerConfig.tokenEndpoint,
            grants = credentialOffer.grants,
            issuerEndpoint = issuerInfo.credentialIssuer,
            credentialEndpoint = issuerInfo.credentialEndpoint,
            credentialConfiguration = credentialConfiguration,
            deferredCredentialEndpoint = issuerInfo.deferredCredentialEndpoint,
            nonceEndpoint = issuerInfo.nonceEndpoint,
            isBatch = issuerCredentialInfo.batchCredentialIssuance != null
        )
    }

    companion object {
        private val supportedBindingMethods = listOf("jwk", "did:jwk")
    }
}
