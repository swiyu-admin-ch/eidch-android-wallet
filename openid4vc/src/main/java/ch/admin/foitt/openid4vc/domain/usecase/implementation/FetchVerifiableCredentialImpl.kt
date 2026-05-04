package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.BatchCredential
import ch.admin.foitt.openid4vc.domain.model.CredentialType
import ch.admin.foitt.openid4vc.domain.model.DeferredCredential
import ch.admin.foitt.openid4vc.domain.model.FetchCredentialResult
import ch.admin.foitt.openid4vc.domain.model.VerifiableCredential
import ch.admin.foitt.openid4vc.domain.model.VerifiableCredentialParams
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CreateCredentialRequestError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchAccessTokenError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchNonceError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchVerifiableCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.Grant
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.toFetchVerifiableCredentialError
import ch.admin.foitt.openid4vc.domain.model.keyBinding.BindingKeyPair
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionType
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository
import ch.admin.foitt.openid4vc.domain.usecase.CreateCredentialRequest
import ch.admin.foitt.openid4vc.domain.usecase.CreateCredentialRequestProofsJwt
import ch.admin.foitt.openid4vc.domain.usecase.DeleteKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.FetchVerifiableCredential
import ch.admin.foitt.openid4vc.utils.retryUseCase
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onFailure
import java.net.URL
import javax.inject.Inject

internal class FetchVerifiableCredentialImpl @Inject constructor(
    private val credentialOfferRepository: CredentialOfferRepository,
    private val createCredentialRequestProofsJwt: CreateCredentialRequestProofsJwt,
    private val createCredentialRequest: CreateCredentialRequest,
    private val deleteKeyPair: DeleteKeyPair,
) : FetchVerifiableCredential {
    override suspend fun invoke(
        verifiableCredentialParams: VerifiableCredentialParams,
        bindingKeyPairs: List<BindingKeyPair>?,
        payloadEncryptionType: PayloadEncryptionType,
    ): Result<FetchCredentialResult, FetchVerifiableCredentialError> = coroutineBinding {
        val tokenResponse = getToken(verifiableCredentialParams.tokenEndpoint, verifiableCredentialParams.grants)
            .bind()
        val proofs = bindingKeyPairs?.let {
            val nonce = verifiableCredentialParams.nonceEndpoint?.let {
                credentialOfferRepository.fetchNonce(it)
                    .mapError(FetchNonceError::toFetchVerifiableCredentialError)
                    .bind()
            }
            retryUseCase {
                createCredentialRequestProofsJwt(
                    keyPairs = bindingKeyPairs,
                    issuer = verifiableCredentialParams.issuerEndpoint.toString(),
                    cNonce = nonce,
                )
            }.bind()
        }
        val credentialRequestType = createCredentialRequest(
            payloadEncryptionType = payloadEncryptionType,
            credentialType = CredentialType.Verifiable(
                verifiableCredentialParams = verifiableCredentialParams,
                proofs = proofs,
            )
        ).mapError(CreateCredentialRequestError::toFetchVerifiableCredentialError)
            .bind()

        val fetchCredentialResponse = credentialOfferRepository.fetchCredential(
            issuerEndpoint = verifiableCredentialParams.credentialEndpoint,
            tokenResponse = tokenResponse,
            credentialRequestType = credentialRequestType,
            payloadEncryptionType = payloadEncryptionType,
        ).bind()

        getCredentialResult(
            credentialResponse = fetchCredentialResponse,
            verifiableCredentialParams = verifiableCredentialParams,
            bindingKeyPairs = bindingKeyPairs,
            accessToken = tokenResponse.accessToken,
            refreshToken = tokenResponse.refreshToken,
        ).bind()
    }.onFailure { deleteHardwareKey(bindingKeyPairs) }

    private suspend fun getToken(tokenEndpoint: URL, grant: Grant) =
        if (grant.preAuthorizedCode != null) {
            credentialOfferRepository.fetchAccessToken(
                tokenEndpoint,
                grant.preAuthorizedCode.preAuthorizedCode
            ).mapError(FetchAccessTokenError::toFetchVerifiableCredentialError)
        } else if (grant.refreshToken != null) {
            credentialOfferRepository.fetchAccessTokenByRefreshToken(
                tokenEndpoint,
                grant.refreshToken
            ).mapError(FetchAccessTokenError::toFetchVerifiableCredentialError)
        } else {
            Err(CredentialOfferError.UnsupportedGrantType)
        }

    private suspend fun getCredentialResult(
        credentialResponse: CredentialResponse,
        verifiableCredentialParams: VerifiableCredentialParams,
        bindingKeyPairs: List<BindingKeyPair>?,
        accessToken: String,
        refreshToken: String?,
    ): Result<FetchCredentialResult, FetchVerifiableCredentialError> = coroutineBinding {
        val keyBindings = bindingKeyPairs?.map { getKeyBinding(it.keyPair) }
        when (credentialResponse) {
            is CredentialResponse.VerifiableCredential -> {
                if (verifiableCredentialParams.isBatch) {
                    BatchCredential(
                        refreshToken = refreshToken,
                        credentialResponse.credentials.mapIndexed { index, credential ->
                            VerifiableCredential(
                                credential.credential,
                                keyBinding = keyBindings?.getOrNull(index),
                            )
                        },
                    )
                } else {
                    VerifiableCredential(
                        credentialResponse.credentials.first().credential,
                        keyBinding = keyBindings?.first()
                    )
                }
            }
            is CredentialResponse.DeferredCredential -> {
                if (verifiableCredentialParams.deferredCredentialEndpoint != null) {
                    DeferredCredential(
                        transactionId = credentialResponse.transactionId,
                        pollInterval = credentialResponse.interval,
                        format = verifiableCredentialParams.credentialConfiguration.format,
                        keyBindings = keyBindings,
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        endpoint = verifiableCredentialParams.deferredCredentialEndpoint,
                    )
                } else {
                    Err(CredentialOfferError.MetadataMisconfiguration("Missing deferred credential endpoint")).bind()
                }
            }
        }
    }

    private fun getKeyBinding(keyPair: JWSKeyPair): KeyBinding {
        val (publicKey, privateKey) = if (keyPair.bindingType == KeyBindingType.SOFTWARE) {
            keyPair.keyPair.public.encoded to keyPair.keyPair.private.encoded
        } else {
            null to null
        }
        return KeyBinding(
            identifier = keyPair.keyId,
            algorithm = keyPair.algorithm,
            bindingType = keyPair.bindingType,
            publicKey = publicKey,
            privateKey = privateKey
        )
    }

    private suspend fun deleteHardwareKey(bindingKeyPairs: List<BindingKeyPair>?) {
        bindingKeyPairs?.forEach { bindingKeyPair ->
            val keyPair = bindingKeyPair.keyPair
            if (keyPair.bindingType == KeyBindingType.HARDWARE) {
                deleteKeyPair(keyPair.keyId)
            }
        }
    }
}
