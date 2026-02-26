package ch.admin.foitt.wallet.platform.appAttestation.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.KeyAttestationJwt
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ValidateKeyAttestationError
import com.github.michaelbull.result.Result

fun interface ValidateKeyAttestation {
    suspend operator fun invoke(
        originalJwk: Jwk,
        keyAttestationJwt: KeyAttestationJwt,
    ): Result<Jwt, ValidateKeyAttestationError>
}
