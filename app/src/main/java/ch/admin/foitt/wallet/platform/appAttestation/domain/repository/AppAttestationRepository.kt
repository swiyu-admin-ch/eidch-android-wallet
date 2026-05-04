package ch.admin.foitt.wallet.platform.appAttestation.domain.repository

import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AppAttestationRepositoryError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationChallengeResponse
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationResponse
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.KeyAttestationResponse
import com.github.michaelbull.result.Result

interface AppAttestationRepository {
    suspend fun fetchChallenge(): Result<AttestationChallengeResponse, AppAttestationRepositoryError>
    suspend fun fetchClientAttestation(
        publicKey: Jwk,
    ): Result<ClientAttestationResponse, AppAttestationRepositoryError>

    suspend fun fetchKeyAttestation(
        publicKey: Jwk,
    ): Result<KeyAttestationResponse, AppAttestationRepositoryError>
}
