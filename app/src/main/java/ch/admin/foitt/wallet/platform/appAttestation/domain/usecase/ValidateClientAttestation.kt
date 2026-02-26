package ch.admin.foitt.wallet.platform.appAttestation.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationResponse
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ValidateClientAttestationError
import com.github.michaelbull.result.Result

interface ValidateClientAttestation {
    suspend operator fun invoke(
        keyStoreAlias: String,
        originalJwk: Jwk,
        clientAttestationResponse: ClientAttestationResponse
    ): Result<ClientAttestation, ValidateClientAttestationError>
}
